package com.michaldrabik.ui_watchlist

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show

data class WatchlistItem(
  override val show: Show,
  val season: Season,
  val episode: Episode,
  val upcomingEpisode: Episode,
  override val image: Image,
  val episodesCount: Int,
  val watchedEpisodesCount: Int,
  override val isLoading: Boolean = false,
  val headerTextResId: Int? = null,
  val isPinned: Boolean = false
) : ListItem {

  fun isSameAs(other: WatchlistItem) =
    show.ids.trakt == other.show.ids.trakt && isHeader() == other.isHeader()

  fun isHeader() = headerTextResId != null

  fun isNew() = episode.firstAired?.isBefore(nowUtc()) ?: false &&
    nowUtcMillis() - (episode.firstAired?.toMillis() ?: 0) < Config.NEW_BADGE_DURATION

  companion object {
    val EMPTY = WatchlistItem(
      Show.EMPTY,
      Season.EMPTY,
      Episode.EMPTY,
      Episode.EMPTY,
      Image.createUnavailable(ImageType.POSTER),
      0,
      0
    )
  }
}
