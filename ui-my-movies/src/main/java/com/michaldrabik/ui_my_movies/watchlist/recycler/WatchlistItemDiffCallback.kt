package com.michaldrabik.ui_my_movies.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback : DiffUtil.ItemCallback<WatchlistListItem>() {

  override fun areItemsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem): Boolean {
    val areMovies = oldItem is WatchlistListItem.MovieItem && newItem is WatchlistListItem.MovieItem
    val areFilters = oldItem is WatchlistListItem.FiltersItem && newItem is WatchlistListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as WatchlistListItem.MovieItem),
        (newItem as WatchlistListItem.MovieItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem): Boolean {
    return when (oldItem) {
      is WatchlistListItem.MovieItem -> areContentsTheSame(oldItem, (newItem as WatchlistListItem.MovieItem))
      is WatchlistListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as WatchlistListItem.FiltersItem))
    }
  }

  private fun areItemsTheSame(
    oldItem: WatchlistListItem.MovieItem,
    newItem: WatchlistListItem.MovieItem,
  ): Boolean {
    return oldItem.movie.traktId == newItem.movie.traktId
  }

  private fun areContentsTheSame(
    oldItem: WatchlistListItem.MovieItem,
    newItem: WatchlistListItem.MovieItem,
  ): Boolean {
    return oldItem.image == newItem.image &&
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
