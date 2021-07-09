package com.michaldrabik.ui_progress.progress.recycler

import androidx.annotation.StringRes
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import java.time.format.DateTimeFormatter
import com.michaldrabik.ui_model.Episode as EpisodeModel

sealed class ProgressListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  data class Episode(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val episode: EpisodeModel?,
    val season: Season?,
    val totalCount: Int,
    val watchedCount: Int,
    val isUpcoming: Boolean,
    val isPinned: Boolean,
    val translations: TranslationsBundle? = null,
    val dateFormat: DateTimeFormatter? = null,
  ) : ProgressListItem(show, image, isLoading) {

    fun isNew() = episode?.firstAired?.isBefore(nowUtc()) ?: false &&
      nowUtcMillis() - (episode?.firstAired?.toMillis() ?: 0) < Config.NEW_BADGE_DURATION

    fun requireEpisode() = episode!!
    fun requireSeason() = season!!
  }

  data class Header(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    @StringRes val textResId: Int,
  ) : ProgressListItem(show, image, isLoading) {

    companion object {
      fun create(@StringRes textResId: Int) =
        Header(
          show = Show.EMPTY,
          image = Image.createUnavailable(ImageType.POSTER),
          textResId = textResId
        )
    }

    override fun isSameAs(other: ListItem) =
      textResId == (other as? Header)?.textResId
  }
}
