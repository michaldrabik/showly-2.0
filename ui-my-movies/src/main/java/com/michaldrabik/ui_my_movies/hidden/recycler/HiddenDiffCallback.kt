package com.michaldrabik.ui_my_movies.hidden.recycler

import androidx.recyclerview.widget.DiffUtil

class HiddenDiffCallback : DiffUtil.ItemCallback<HiddenListItem>() {

  override fun areItemsTheSame(oldItem: HiddenListItem, newItem: HiddenListItem): Boolean {
    val areMovies = oldItem is HiddenListItem.MovieItem && newItem is HiddenListItem.MovieItem
    val areFilters = oldItem is HiddenListItem.FiltersItem && newItem is HiddenListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as HiddenListItem.MovieItem),
        (newItem as HiddenListItem.MovieItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: HiddenListItem, newItem: HiddenListItem): Boolean {
    return when (oldItem) {
      is HiddenListItem.MovieItem -> areContentsTheSame(oldItem, (newItem as HiddenListItem.MovieItem))
      is HiddenListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as HiddenListItem.FiltersItem))
    }
  }

  private fun areItemsTheSame(
    oldItem: HiddenListItem.MovieItem,
    newItem: HiddenListItem.MovieItem,
  ): Boolean {
    return oldItem.movie.traktId == newItem.movie.traktId
  }

  private fun areContentsTheSame(
    oldItem: HiddenListItem.MovieItem,
    newItem: HiddenListItem.MovieItem,
  ): Boolean {
    return oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
  }

  private fun areContentsTheSame(
    oldItem: HiddenListItem.FiltersItem,
    newItem: HiddenListItem.FiltersItem,
  ): Boolean {
    return oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
