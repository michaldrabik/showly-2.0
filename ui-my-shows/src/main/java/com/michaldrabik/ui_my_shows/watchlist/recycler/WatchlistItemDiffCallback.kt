package com.michaldrabik.ui_my_shows.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback : DiffUtil.ItemCallback<WatchlistListItem>() {

  override fun areItemsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem): Boolean {
    val areMovies = oldItem is WatchlistListItem.ShowItem && newItem is WatchlistListItem.ShowItem
    val areFilters = oldItem is WatchlistListItem.FiltersItem && newItem is WatchlistListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as WatchlistListItem.ShowItem),
        (newItem as WatchlistListItem.ShowItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem): Boolean {
    return when (oldItem) {
      is WatchlistListItem.ShowItem -> areContentsTheSame(oldItem, (newItem as WatchlistListItem.ShowItem))
      is WatchlistListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as WatchlistListItem.FiltersItem))
    }
  }

  private fun areItemsTheSame(
    oldItem: WatchlistListItem.ShowItem,
    newItem: WatchlistListItem.ShowItem,
  ): Boolean {
    return oldItem.show.traktId == newItem.show.traktId
  }

  private fun areContentsTheSame(
    oldItem: WatchlistListItem.ShowItem,
    newItem: WatchlistListItem.ShowItem,
  ): Boolean {
    return oldItem.show.firstAired == newItem.show.firstAired &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
  }

  private fun areContentsTheSame(
    oldItem: WatchlistListItem.FiltersItem,
    newItem: WatchlistListItem.FiltersItem,
  ): Boolean {
    return oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
