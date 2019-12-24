package com.michaldrabik.showly2.ui.watchlist.recycler

import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.showly2.utilities.extensions.toMillis

data class WatchlistItem(
  override val show: Show,
  val season: Season,
  val episode: Episode,
  override val image: Image,
  val episodesCount: Int,
  val watchedEpisodesCount: Int,
  override val isLoading: Boolean = false,
  val headerTextResId: Int? = null
) : ListItem {

  fun isSameAs(other: WatchlistItem) =
    show.ids.trakt == other.show.ids.trakt && isHeader() == other.isHeader()

  fun isHeader() = headerTextResId != null

  fun isNew() = episode.firstAired?.isBefore(nowUtc()) ?: false &&
      nowUtcMillis() - (episode.firstAired?.toMillis() ?: 0) < Config.NEW_BADGE_DURATION
}
