package com.michaldrabik.ui_my_shows.archive.recycler

import androidx.recyclerview.widget.DiffUtil

class ArchiveDiffCallback : DiffUtil.ItemCallback<ArchiveListItem>() {

  override fun areItemsTheSame(oldItem: ArchiveListItem, newItem: ArchiveListItem): Boolean {
    val areMovies = oldItem is ArchiveListItem.ShowItem && newItem is ArchiveListItem.ShowItem
    val areFilters = oldItem is ArchiveListItem.FiltersItem && newItem is ArchiveListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as ArchiveListItem.ShowItem),
        (newItem as ArchiveListItem.ShowItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: ArchiveListItem, newItem: ArchiveListItem): Boolean {
    return when (oldItem) {
      is ArchiveListItem.ShowItem -> areContentsTheSame(oldItem, (newItem as ArchiveListItem.ShowItem))
      is ArchiveListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as ArchiveListItem.FiltersItem))
    }
  }

  private fun areItemsTheSame(
    oldItem: ArchiveListItem.ShowItem,
    newItem: ArchiveListItem.ShowItem,
  ): Boolean {
    return oldItem.show.traktId == newItem.show.traktId
  }

  private fun areContentsTheSame(
    oldItem: ArchiveListItem.ShowItem,
    newItem: ArchiveListItem.ShowItem,
  ): Boolean {
    return oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
  }

  private fun areContentsTheSame(
    oldItem: ArchiveListItem.FiltersItem,
    newItem: ArchiveListItem.FiltersItem,
  ): Boolean {
    return oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
