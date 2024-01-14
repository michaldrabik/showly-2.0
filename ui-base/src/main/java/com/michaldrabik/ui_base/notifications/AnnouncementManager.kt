package com.michaldrabik.ui_base.notifications

import android.content.Context
import androidx.work.WorkManager
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.OnHoldItemsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.notifications.schedulers.MovieAnnouncementScheduler
import com.michaldrabik.ui_base.notifications.schedulers.MovieAnnouncementScheduler.Companion.ANNOUNCEMENT_MOVIE_WORK_TAG
import com.michaldrabik.ui_base.notifications.schedulers.ShowAnnouncementScheduler
import com.michaldrabik.ui_base.notifications.schedulers.ShowAnnouncementScheduler.Companion.ANNOUNCEMENT_WORK_TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val mappers: Mappers,
  private val localSource: LocalDataSource,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val showAnnouncementScheduler: ShowAnnouncementScheduler,
  private val movieAnnouncementScheduler: MovieAnnouncementScheduler,
) {

  companion object {
    private const val MOVIE_MIN_THRESHOLD_DAYS = 30
    private const val MOVIE_THRESHOLD_HOUR = 12
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun refreshShowsAnnouncements() {
    Timber.d("Refreshing shows announcements")

    val now = nowUtc()
    val nowMillis = now.toMillis()
    val limit = now.plusMonths(3)
    WorkManager.getInstance(context).cancelAllWorkByTag(ANNOUNCEMENT_WORK_TAG)
    Timber.d("Current time: ${logFormatter.format(now)} UTC")

    val settings = settingsRepository.load()
    if (!settings.episodesNotificationsEnabled) {
      Timber.d("Episodes announcements are disabled. Exiting...")
      return
    }

    val myShows = localSource.myShows.getAll()
    val watchlistShows = localSource.watchlistShows.getAll()
    if (myShows.isEmpty() && watchlistShows.isEmpty()) {
      Timber.d("Nothing to process. Exiting...")
      return
    }

    val language = translationsRepository.getLanguage()
    val delay = settings.episodesNotificationsDelay
    val onHoldIds = onHoldItemsRepository.getAll().map { it.id }

    myShows
      .forEach { show ->
        Timber.d("Processing ${show.title} (${show.idTrakt})")

        if (onHoldIds.contains(show.idTrakt)) {
          Timber.d("${show.title} (${show.idTrakt}) is on hold. Skipping...")
          return@forEach
        }

        val fromTime = if (delay.isBefore()) nowMillis else nowMillis - delay.delayMs
        val episode = localSource.episodes.getFirstUnwatched(show.idTrakt, fromTime, limit.toMillis())
        episode?.firstAired?.let { airDate ->
          when {
            delay.isBefore() -> {
              if (airDate.toMillis() + delay.delayMs >= nowMillis) {
                showAnnouncementScheduler.scheduleAnnouncement(
                  showDb = show,
                  episodeNumber = episode.episodeNumber,
                  episodeSeasonNumber = episode.seasonNumber,
                  episodeDate = episode.firstAired!!,
                  delay = delay,
                  language = language
                )
              } else {
                Timber.d("Time with delay included has already passed.")
              }
            }
            else -> {
              showAnnouncementScheduler.scheduleAnnouncement(
                showDb = show,
                episodeNumber = episode.episodeNumber,
                episodeSeasonNumber = episode.seasonNumber,
                episodeDate = episode.firstAired!!,
                delay = delay,
                language = language
              )
            }
          }
        }
      }

    for (show in watchlistShows) {
      Timber.d("Processing Watchlist ${show.title} (${show.idTrakt})")

      val fromTime = if (delay.isBefore()) nowMillis else nowMillis - delay.delayMs
      val airDate = show.firstAired.toZonedDateTime() ?: ZonedDateTime.now().minusYears(1)

      if (airDate.toMillis() <= fromTime) {
        continue
      }

      if (delay.isBefore()) {
        if (airDate.toMillis() + delay.delayMs >= nowMillis) {
          showAnnouncementScheduler.scheduleAnnouncement(
            showDb = show,
            episodeNumber = 1,
            episodeSeasonNumber = 1,
            episodeDate = airDate,
            delay = delay,
            language = language
          )
        } else {
          Timber.d("Time with delay included has already passed.")
        }
      } else {
        showAnnouncementScheduler.scheduleAnnouncement(
          showDb = show,
          episodeNumber = 1,
          episodeSeasonNumber = 1,
          episodeDate = airDate,
          delay = delay,
          language = language
        )
      }
    }
  }

  suspend fun refreshMoviesAnnouncements() {
    Timber.d("Refreshing movies announcements")

    val now = nowUtc()
    Timber.d("Current time: ${logFormatter.format(now)} UTC")

    WorkManager.getInstance(context).cancelAllWorkByTag(ANNOUNCEMENT_MOVIE_WORK_TAG)

    if (!settingsRepository.isMoviesEnabled) {
      Timber.d("Movies disabled. Skipping...")
      return
    }

    val movies = localSource.watchlistMovies.getAll()
      .map { mappers.movie.fromDatabase(it) }

    if (movies.isEmpty()) {
      Timber.d("Nothing to process. Exiting...")
      return
    }

    val language = translationsRepository.getLanguage()
    movies
      .filter {
        Timber.d("Processing ${it.title} (${it.traktId})")
        it.released != null &&
          (!it.hasAired() || it.isToday()) &&
          it.released!!.toEpochDay() - nowUtcDay().toEpochDay() < MOVIE_MIN_THRESHOLD_DAYS &&
          ZonedDateTime.now().hour < MOVIE_THRESHOLD_HOUR // We want movies notifications to come out the release day at 12:00 local time
      }
      .forEach {
        movieAnnouncementScheduler.scheduleAnnouncement(context, it, language)
      }
  }
}
