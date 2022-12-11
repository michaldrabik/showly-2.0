package com.michaldrabik.ui_my_shows.common.recycler

import androidx.recyclerview.widget.DiffUtil

class CollectionItemDiffCallback : DiffUtil.ItemCallback<CollectionListItem>() {

  override fun areItemsTheSame(oldItem: CollectionListItem, newItem: CollectionListItem): Boolean {
    val areMovies = oldItem is CollectionListItem.ShowItem && newItem is CollectionListItem.ShowItem
    val areFilters = oldItem is CollectionListItem.FiltersItem && newItem is CollectionListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as CollectionListItem.ShowItem),
        (newItem as CollectionListItem.ShowItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: CollectionListItem, newItem: CollectionListItem): Boolean {
    return when (oldItem) {
      is CollectionListItem.ShowItem -> areContentsTheSame(oldItem, (newItem as CollectionListItem.ShowItem))
      is CollectionListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as CollectionListItem.FiltersItem))
    }
  }

  private fun areItemsTheSame(
    oldItem: CollectionListItem.ShowItem,
    newItem: CollectionListItem.ShowItem,
  ): Boolean {
    return oldItem.show.traktId == newItem.show.traktId
  }

  private fun areContentsTheSame(
    oldItem: CollectionListItem.ShowItem,
    newItem: CollectionListItem.ShowItem,
  ): Boolean {
    return oldItem.show.firstAired == newItem.show.firstAired &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.sortOrder == newItem.sortOrder &&
      oldItem.userRating == newItem.userRating
  }

  private fun areContentsTheSame(
    oldItem: CollectionListItem.FiltersItem,
    newItem: CollectionListItem.FiltersItem,
  ): Boolean {
    return oldItem.isUpcoming == newItem.isUpcoming &&
      oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
