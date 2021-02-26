package com.michaldrabik.ui_base.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_CHANNEL
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_CONTENT
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_IMAGE_URL
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_THEME
import com.michaldrabik.ui_base.notifications.AnnouncementWorker.Companion.DATA_TITLE
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

@AppScope
class AnnouncementManager @Inject constructor(
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
  private val mappers: Mappers
) {

  companion object {
    private const val ANNOUNCEMENT_WORK_TAG = "ANNOUNCEMENT_WORK_TAG"
    private const val ANNOUNCEMENT_MOVIE_WORK_TAG = "ANNOUNCEMENT_MOVIE_WORK_TAG"
    private const val ANNOUNCEMENT_STATIC_DELAY_MS = 60000 // 1 min
    private const val MOVIE_MIN_THRESHOLD_DAYS = 30
    private const val MOVIE_THRESHOLD_HOUR = 12
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun refreshShowsAnnouncements(context: Context) {
    Timber.i("Refreshing shows announcements")

    val now = nowUtcMillis()
    WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(ANNOUNCEMENT_WORK_TAG)

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

    val delay = settings.episodesNotificationsDelay
    myShows.forEach { show ->
      Timber.i("Processing ${show.title} (${show.idTrakt})")
      val episodes = database.episodesDao().getAllByShowId(show.idTrakt)
      episodes
        .filter { it.seasonNumber != 0 && !it.isWatched }
        .filter { it.firstAired != null && (it.firstAired!!.toMillis() + delay.delayMs) > now }
        .minBy { it.firstAired!!.toMillis() }
        ?.let {
          scheduleAnnouncement(context.applicationContext, show, it, delay)
        }
    }
  }

  suspend fun refreshMoviesAnnouncements(context: Context) {
    Timber.i("Refreshing movies announcements")

    WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(ANNOUNCEMENT_MOVIE_WORK_TAG)

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

    movies
      .filter {
        Timber.i("Processing ${it.title} (${it.traktId})")
        it.released != null &&
          (!it.hasAired() || it.isToday()) &&
          it.released!!.toEpochDay() - nowUtcDay().toEpochDay() < MOVIE_MIN_THRESHOLD_DAYS &&
          ZonedDateTime.now().hour < MOVIE_THRESHOLD_HOUR // We want movies notifications to come out the release day at 12:00 local time
      }
      .forEach {
        scheduleAnnouncement(context.applicationContext, it)
      }
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private suspend fun scheduleAnnouncement(
    context: Context,
    showDb: Show,
    episodeDb: Episode,
    delay: NotificationDelay
  ) {
    val show = mappers.show.fromDatabase(showDb)

    val data = Data.Builder().apply {
      putString(DATA_CHANNEL, NotificationChannel.EPISODES_ANNOUNCEMENTS.name)
      putString(DATA_TITLE, "${show.title} - Season ${episodeDb.seasonNumber}")
      putInt(DATA_THEME, settingsRepository.theme)

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

    WorkManager.getInstance(context.applicationContext).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.i("Notification set for ${show.title}: $logTime UTC")
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private suspend fun scheduleAnnouncement(
    context: Context,
    movie: Movie
  ) {
    val data = Data.Builder().apply {
      putString(DATA_CHANNEL, NotificationChannel.MOVIES_ANNOUNCEMENTS.name)
      putString(DATA_TITLE, movie.title)
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

    WorkManager.getInstance(context.applicationContext).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.i("Notification set for ${movie.title}: $logTime UTC")
  }
}
