package com.michaldrabik.ui_progress.progress.recycler

import androidx.recyclerview.widget.DiffUtil

class ProgressItemDiffCallback : DiffUtil.ItemCallback<ProgressListItem>() {

  override fun areItemsTheSame(oldItem: ProgressListItem, newItem: ProgressListItem): Boolean {
    val areEpisodes = oldItem is ProgressListItem.Episode && newItem is ProgressListItem.Episode
    val areHeaders = oldItem is ProgressListItem.Header && newItem is ProgressListItem.Header
    return when {
      areEpisodes -> areItemsTheSame(
        (oldItem as ProgressListItem.Episode),
        (newItem as ProgressListItem.Episode)
      )
      areHeaders -> areItemsTheSame(
        (oldItem as ProgressListItem.Header),
        (newItem as ProgressListItem.Header)
      )
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: ProgressListItem, newItem: ProgressListItem) =
    when (oldItem) {
      is ProgressListItem.Episode -> areContentsTheSame(oldItem, (newItem as ProgressListItem.Episode))
      is ProgressListItem.Header -> areContentsTheSame(oldItem, (newItem as ProgressListItem.Header))
    }

  private fun areItemsTheSame(oldItem: ProgressListItem.Episode, newItem: ProgressListItem.Episode) =
    oldItem.show.traktId == newItem.show.traktId

  private fun areItemsTheSame(oldItem: ProgressListItem.Header, newItem: ProgressListItem.Header) =
    oldItem.textResId == newItem.textResId

  private fun areContentsTheSame(oldItem: ProgressListItem.Episode, newItem: ProgressListItem.Episode) =
    oldItem.show.traktId == newItem.show.traktId &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.watchedCount == newItem.watchedCount &&
      oldItem.totalCount == newItem.totalCount &&
      oldItem.episode == newItem.episode &&
      oldItem.translations == newItem.translations &&
      oldItem.isUpcoming == newItem.isUpcoming &&
      oldItem.isPinned == newItem.isPinned

  private fun areContentsTheSame(oldItem: ProgressListItem.Header, newItem: ProgressListItem.Header) =
    oldItem.textResId == newItem.textResId
}
