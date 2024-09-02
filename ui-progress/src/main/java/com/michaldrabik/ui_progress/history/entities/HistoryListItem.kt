package com.michaldrabik.ui_progress.history.entities

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.michaldrabik.ui_model.Episode as EpisodeUi

internal sealed class HistoryListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  data class Episode(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val episode: EpisodeUi,
    val season: Season,
    val translations: TranslationsBundle? = null,
    val dateFormat: DateTimeFormatter? = null,
  ) : HistoryListItem(show, image, isLoading) {

    override fun isSameAs(other: ListItem): Boolean {
      return episode.ids.trakt == (other as? Episode)?.episode?.ids?.trakt
    }
  }

  data class Header(
    val date: LocalDateTime,
    val language: String,
  ) : HistoryListItem(
      show = Show.EMPTY,
      image = Image.createUnknown(ImageType.POSTER),
      isLoading = false,
    ) {
    override fun isSameAs(other: ListItem): Boolean {
      val otherHeader = (other as? Header) ?: return false
      return date.isEqual(otherHeader.date)
    }
  }

  data class Filters(
    val period: HistoryPeriod,
  ) : HistoryListItem(
      show = Show.EMPTY,
      image = Image.createUnknown(ImageType.POSTER),
      isLoading = false,
    ) {
    override fun isSameAs(other: ListItem): Boolean {
      return period == (other as? Filters)?.period
    }
  }
}
