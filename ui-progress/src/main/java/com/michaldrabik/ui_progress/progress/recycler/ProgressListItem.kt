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
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
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
    val isOnHold: Boolean,
    val translations: TranslationsBundle? = null,
    val dateFormat: DateTimeFormatter? = null,
    val sortOrder: SortOrder? = null,
    val userRating: Int? = null,
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
    val type: Type,
    @StringRes val textResId: Int,
    val isCollapsed: Boolean,
  ) : ProgressListItem(show, image, isLoading) {

    companion object {
      fun create(
        type: Type,
        @StringRes textResId: Int,
        isCollapsed: Boolean,
      ) = Header(
        type = type,
        show = Show.EMPTY,
        image = Image.createUnavailable(ImageType.POSTER),
        textResId = textResId,
        isCollapsed = isCollapsed
      )
    }

    override fun isSameAs(other: ListItem) =
      textResId == (other as? Header)?.textResId

    enum class Type {
      UPCOMING,
      ON_HOLD
    }
  }

  data class Filters(
    val sortOrder: SortOrder,
    val sortType: SortType,
  ) : ProgressListItem(
    show = Show.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false
  )
}
