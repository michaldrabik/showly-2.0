package com.michaldrabik.ui_base.notifications.schedulers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.notifications.AnnouncementWorker
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MovieAnnouncementScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val moviesImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
) {

  companion object {
    const val ANNOUNCEMENT_MOVIE_WORK_TAG = "ANNOUNCEMENT_MOVIE_WORK_TAG"
    private const val MOVIE_THRESHOLD_HOUR = 12
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun scheduleAnnouncement(
    context: Context,
    movie: Movie,
    language: String,
  ) {
    var translation: Translation? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
    }

    val data = Data.Builder().apply {
      putLong(AnnouncementWorker.DATA_MOVIE_ID, movie.traktId)
      putString(AnnouncementWorker.DATA_CHANNEL, NotificationChannel.MOVIES_ANNOUNCEMENTS.name)
      putString(AnnouncementWorker.DATA_TITLE, if (translation?.hasTitle == true) translation.title else movie.title)
      putString(AnnouncementWorker.DATA_CONTENT, context.getString(R.string.textNewMovieAvailable))
      putInt(AnnouncementWorker.DATA_THEME, settingsRepository.theme)

      val posterImage = moviesImagesProvider.findCachedImage(movie, ImageType.POSTER)
      if (posterImage.status == ImageStatus.AVAILABLE) {
        putString(AnnouncementWorker.DATA_IMAGE_URL, posterImage.fullFileUrl)
      } else {
        val fanartImage = moviesImagesProvider.findCachedImage(movie, ImageType.FANART)
        if (fanartImage.status == ImageStatus.AVAILABLE) {
          putString(AnnouncementWorker.DATA_IMAGE_URL, fanartImage.fullFileUrl)
        }
      }
    }

    val now = ZonedDateTime.now()
    val days = movie.released!!.toEpochDay() - nowUtcDay().toEpochDay()
    val offset = now.withHour(MOVIE_THRESHOLD_HOUR).withMinute(0).toMillis() - now.toMillis()
    val delayed = (days * TimeUnit.DAYS.toMillis(1)) + offset
    val request = OneTimeWorkRequestBuilder<AnnouncementWorker>()
      .setInputData(data.build())
      .setInitialDelay(delayed, TimeUnit.MILLISECONDS)
      .addTag(ANNOUNCEMENT_MOVIE_WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.d("Notification set for ${movie.title}: $logTime UTC")
  }
}
