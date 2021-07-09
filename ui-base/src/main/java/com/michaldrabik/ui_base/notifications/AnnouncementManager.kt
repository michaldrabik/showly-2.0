package com.michaldrabik.ui_base.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_CHANNEL
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_CONTENT
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_IMAGE_URL
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_MOVIE_ID
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_SHOW_ID
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_THEME
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_TITLE
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val mappers: Mappers,
) {

  companion object {
    private const val ANNOUNCEMENT_WORK_TAG = "ANNOUNCEMENT_WORK_TAG"
    private const val ANNOUNCEMENT_MOVIE_WORK_TAG = "ANNOUNCEMENT_MOVIE_WORK_TAG"
    private const val ANNOUNCEMENT_STATIC_DELAY_MS = 60000 // 1 min
    private const val MOVIE_MIN_THRESHOLD_DAYS = 30
    private const val MOVIE_THRESHOLD_HOUR = 12
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun refreshShowsAnnouncements() {
    Timber.i("Refreshing shows announcements")

    val now = nowUtc()
    val nowMillis = now.toMillis()
    val limit = now.plusMonths(3)
    WorkManager.getInstance(context).cancelAllWorkByTag(ANNOUNCEMENT_WORK_TAG)
    Timber.i("Current time: ${logFormatter.format(now)} UTC")

    val settings = settingsRepository.load()
    if (!settings.episodesNotificationsEnabled) {
      Timber.i("Episodes announcements are disabled. Exiting...")
      return
    }

    val myShows = database.myShowsDao().getAll()
    if (myShows.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return
    }

    val language = translationsRepository.getLanguage()
    val delay = settings.episodesNotificationsDelay
    myShows.forEach { show ->
      Timber.i("Processing ${show.title} (${show.idTrakt})")
      val fromTime = if (delay.isBefore()) nowMillis else nowMillis - delay.delayMs
      val episode = database.episodesDao().getFirstUnwatched(show.idTrakt, fromTime, limit.toMillis())
      episode?.firstAired?.let { airDate ->
        when {
          delay.isBefore() -> {
            if ((airDate.toMillis() - nowUtcMillis()) + delay.delayMs > 0) {
              scheduleAnnouncement(show, episode, delay, language)
            } else {
              Timber.i("Time with delay included has already passed.")
            }
          }
          else -> {
            scheduleAnnouncement(show, episode, delay, language)
          }
        }
      }
    }
  }

  suspend fun refreshMoviesAnnouncements() {
    Timber.i("Refreshing movies announcements")

    WorkManager.getInstance(context).cancelAllWorkByTag(ANNOUNCEMENT_MOVIE_WORK_TAG)

    if (!settingsRepository.isMoviesEnabled) {
      Timber.i("Movies disabled. Skipping...")
      return
    }

    val movies = database.watchlistMoviesDao().getAll()
      .map { mappers.movie.fromDatabase(it) }

    if (movies.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return
    }

    val language = translationsRepository.getLanguage()
    movies
      .filter {
        Timber.i("Processing ${it.title} (${it.traktId})")
        it.released != null &&
          (!it.hasAired() || it.isToday()) &&
          it.released!!.toEpochDay() - nowUtcDay().toEpochDay() < MOVIE_MIN_THRESHOLD_DAYS &&
          ZonedDateTime.now().hour < MOVIE_THRESHOLD_HOUR // We want movies notifications to come out the release day at 12:00 local time
      }
      .forEach {
        scheduleAnnouncement(context, it, language)
      }
  }

  private suspend fun scheduleAnnouncement(
    showDb: Show,
    episodeDb: Episode,
    delay: NotificationDelay,
    language: String,
  ) {
    val show = mappers.show.fromDatabase(showDb)

    var translation: Translation? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      translation = translationsRepository.loadTranslation(show, language, onlyLocal = true)
    }

    val data = Data.Builder().apply {
      val title = if (translation?.hasTitle == true) translation.title else show.title
      val season = context.getString(R.string.textSeason, episodeDb.seasonNumber)
      val episode = context.getString(R.string.textEpisode, episodeDb.episodeNumber)

      putLong(DATA_SHOW_ID, showDb.idTrakt)
      putString(DATA_TITLE, "$title - $season $episode")
      putInt(DATA_THEME, settingsRepository.theme)
      putString(DATA_CHANNEL, NotificationChannel.EPISODES_ANNOUNCEMENTS.name)

      val stringResId = when (episodeDb.episodeNumber) {
        1 -> if (delay.isBefore()) R.string.textNewSeasonAvailableSoon else R.string.textNewSeasonAvailable
        else -> if (delay.isBefore()) R.string.textNewEpisodeAvailableSoon else R.string.textNewEpisodeAvailable
      }
      putString(DATA_CONTENT, context.getString(stringResId))

      val posterImage = showsImagesProvider.findCachedImage(show, POSTER)
      if (posterImage.status == AVAILABLE) {
        putString(DATA_IMAGE_URL, posterImage.fullFileUrl)
      } else {
        val fanartImage = showsImagesProvider.findCachedImage(show, FANART)
        if (fanartImage.status == AVAILABLE) {
          putString(DATA_IMAGE_URL, fanartImage.fullFileUrl)
        }
      }
    }

    val delayed = (episodeDb.firstAired!!.toMillis() - nowUtcMillis()) + delay.delayMs + ANNOUNCEMENT_STATIC_DELAY_MS
    val request = OneTimeWorkRequestBuilder<AnnouncementWorker>()
      .setInputData(data.build())
      .setInitialDelay(delayed, MILLISECONDS)
      .addTag(ANNOUNCEMENT_WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.i("Notification set for ${show.title}: $logTime UTC")
  }

  private suspend fun scheduleAnnouncement(
    context: Context,
    movie: Movie,
    language: String,
  ) {
    var translation: Translation? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
    }

    val data = Data.Builder().apply {
      putLong(DATA_MOVIE_ID, movie.traktId)
      putString(DATA_CHANNEL, NotificationChannel.MOVIES_ANNOUNCEMENTS.name)
      putString(DATA_TITLE, if (translation?.hasTitle == true) translation.title else movie.title)
      putString(DATA_CONTENT, context.getString(R.string.textNewMovieAvailable))
      putInt(DATA_THEME, settingsRepository.theme)

      val posterImage = moviesImagesProvider.findCachedImage(movie, POSTER)
      if (posterImage.status == AVAILABLE) {
        putString(DATA_IMAGE_URL, posterImage.fullFileUrl)
      } else {
        val fanartImage = moviesImagesProvider.findCachedImage(movie, FANART)
        if (fanartImage.status == AVAILABLE) {
          putString(DATA_IMAGE_URL, fanartImage.fullFileUrl)
        }
      }
    }

    val now = ZonedDateTime.now()
    val days = movie.released!!.toEpochDay() - nowUtcDay().toEpochDay()
    val offset = now.withHour(MOVIE_THRESHOLD_HOUR).withMinute(0).toMillis() - now.toMillis()
    val delayed = (days * TimeUnit.DAYS.toMillis(1)) + offset
    val request = OneTimeWorkRequestBuilder<AnnouncementWorker>()
      .setInputData(data.build())
      .setInitialDelay(delayed, MILLISECONDS)
      .addTag(ANNOUNCEMENT_MOVIE_WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.i("Notification set for ${movie.title}: $logTime UTC")
  }
}
