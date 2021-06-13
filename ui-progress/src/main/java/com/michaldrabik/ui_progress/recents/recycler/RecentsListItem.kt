package com.michaldrabik.ui_progress.recents.recycler

import androidx.annotation.StringRes
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import org.threeten.bp.format.DateTimeFormatter
import com.michaldrabik.ui_model.Episode as EpisodeModel

sealed class RecentsListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  data class Episode(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val episode: EpisodeModel,
    val season: Season,
    val isWatched: Boolean,
    val translations: TranslationsBundle? = null,
    val dateFormat: DateTimeFormatter? = null,
  ) : RecentsListItem(show, image, isLoading) {

    override fun isSameAs(other: ListItem) =
      episode.ids.trakt == (other as? Episode)?.episode?.ids?.trakt
  }

  data class Header(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    @StringRes val textResId: Int,
  ) : RecentsListItem(show, image, isLoading) {

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
