package com.michaldrabik.ui_base.notifications.schedulers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.notifications.AnnouncementWorker
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowAnnouncementScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val mappers: Mappers,
) {

  companion object {
    const val ANNOUNCEMENT_WORK_TAG = "ANNOUNCEMENT_WORK_TAG"
    private const val ANNOUNCEMENT_STATIC_DELAY_MS = 60000 // 1 min
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun scheduleAnnouncement(
    showDb: Show,
    episodeNumber: Int,
    episodeSeasonNumber: Int,
    episodeDate: ZonedDateTime,
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
      val episode = context.getString(R.string.textSeasonEpisode, episodeSeasonNumber, episodeNumber)

      putLong(AnnouncementWorker.DATA_SHOW_ID, showDb.idTrakt)
      putString(AnnouncementWorker.DATA_TITLE, "$title - $episode")
      putInt(AnnouncementWorker.DATA_THEME, settingsRepository.theme)
      putString(AnnouncementWorker.DATA_CHANNEL, NotificationChannel.EPISODES_ANNOUNCEMENTS.name)

      val stringResId = when (episodeNumber) {
        1 -> if (delay.isBefore()) R.string.textNewSeasonAvailableSoon else R.string.textNewSeasonAvailable
        else -> if (delay.isBefore()) R.string.textNewEpisodeAvailableSoon else R.string.textNewEpisodeAvailable
      }
      putString(AnnouncementWorker.DATA_CONTENT, context.getString(stringResId))

      val posterImage = showsImagesProvider.findCachedImage(show, ImageType.POSTER)
      if (posterImage.status == ImageStatus.AVAILABLE) {
        putString(AnnouncementWorker.DATA_IMAGE_URL, posterImage.fullFileUrl)
      } else {
        val fanartImage = showsImagesProvider.findCachedImage(show, ImageType.FANART)
        if (fanartImage.status == ImageStatus.AVAILABLE) {
          putString(AnnouncementWorker.DATA_IMAGE_URL, fanartImage.fullFileUrl)
        }
      }
    }

    val delayed = (episodeDate.toMillis() - nowUtcMillis()) + delay.delayMs + ANNOUNCEMENT_STATIC_DELAY_MS
    val request = OneTimeWorkRequestBuilder<AnnouncementWorker>()
      .setInputData(data.build())
      .setInitialDelay(delayed, TimeUnit.MILLISECONDS)
      .addTag(ANNOUNCEMENT_WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.d("Notification set for ${show.title}: $logTime UTC")
  }
}
