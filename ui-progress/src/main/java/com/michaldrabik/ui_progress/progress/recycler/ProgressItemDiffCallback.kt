package com.michaldrabik.ui_progress.progress.recycler

import androidx.recyclerview.widget.DiffUtil

class ProgressItemDiffCallback : DiffUtil.ItemCallback<ProgressListItem>() {

  override fun areItemsTheSame(oldItem: ProgressListItem, newItem: ProgressListItem): Boolean {
    val areEpisodes = oldItem is ProgressListItem.Episode && newItem is ProgressListItem.Episode
    val areHeaders = oldItem is ProgressListItem.Header && newItem is ProgressListItem.Header
    val areFilters = oldItem is ProgressListItem.Filters && newItem is ProgressListItem.Filters
    return when {
      areEpisodes -> areItemsTheSame(
        (oldItem as ProgressListItem.Episode),
        (newItem as ProgressListItem.Episode)
      )
      areHeaders -> areItemsTheSame(
        (oldItem as ProgressListItem.Header),
        (newItem as ProgressListItem.Header)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: ProgressListItem, newItem: ProgressListItem) =
    when (oldItem) {
      is ProgressListItem.Episode -> areContentsTheSame(oldItem, (newItem as ProgressListItem.Episode))
      is ProgressListItem.Header -> areContentsTheSame(oldItem, (newItem as ProgressListItem.Header))
      is ProgressListItem.Filters -> areContentsTheSame(oldItem, (newItem as ProgressListItem.Filters))
    }

  private fun areItemsTheSame(oldItem: ProgressListItem.Episode, newItem: ProgressListItem.Episode) =
    oldItem.show.traktId == newItem.show.traktId

  private fun areItemsTheSame(oldItem: ProgressListItem.Header, newItem: ProgressListItem.Header) =
    oldItem.textResId == newItem.textResId &&
      oldItem.type == newItem.type

  private fun areContentsTheSame(oldItem: ProgressListItem.Episode, newItem: ProgressListItem.Episode) =
    oldItem.show.traktId == newItem.show.traktId &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.watchedCount == newItem.watchedCount &&
      oldItem.totalCount == newItem.totalCount &&
      oldItem.episode == newItem.episode &&
      oldItem.translations == newItem.translations &&
      oldItem.isUpcoming == newItem.isUpcoming &&
      oldItem.sortOrder == newItem.sortOrder &&
      oldItem.isOnHold == newItem.isOnHold &&
      oldItem.userRating == newItem.userRating &&
      oldItem.isPinned == newItem.isPinned

  private fun areContentsTheSame(oldItem: ProgressListItem.Header, newItem: ProgressListItem.Header) =
    oldItem.textResId == newItem.textResId &&
      oldItem.isCollapsed == newItem.isCollapsed

  private fun areContentsTheSame(
    oldItem: ProgressListItem.Filters,
    newItem: ProgressListItem.Filters,
  ): Boolean {
    return oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
