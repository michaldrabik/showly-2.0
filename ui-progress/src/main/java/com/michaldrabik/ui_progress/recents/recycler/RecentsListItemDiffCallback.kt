package com.michaldrabik.ui_progress.recents.recycler

import androidx.recyclerview.widget.DiffUtil

class RecentsListItemDiffCallback : DiffUtil.ItemCallback<RecentsListItem>() {

  override fun areItemsTheSame(oldItem: RecentsListItem, newItem: RecentsListItem): Boolean {
    val areEpisodes = oldItem is RecentsListItem.Episode && newItem is RecentsListItem.Episode
    val areHeaders = oldItem is RecentsListItem.Header && newItem is RecentsListItem.Header
    return when {
      areEpisodes -> areItemsTheSame(
        (oldItem as RecentsListItem.Episode),
        (newItem as RecentsListItem.Episode)
      )
      areHeaders -> areItemsTheSame(
        (oldItem as RecentsListItem.Header),
        (newItem as RecentsListItem.Header)
      )
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: RecentsListItem, newItem: RecentsListItem) =
    when (oldItem) {
      is RecentsListItem.Episode -> areContentsTheSame(oldItem, (newItem as RecentsListItem.Episode))
      is RecentsListItem.Header -> areContentsTheSame(oldItem, (newItem as RecentsListItem.Header))
    }

  private fun areItemsTheSame(oldItem: RecentsListItem.Episode, newItem: RecentsListItem.Episode) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt

  private fun areItemsTheSame(oldItem: RecentsListItem.Header, newItem: RecentsListItem.Header) =
    oldItem.textResId == newItem.textResId

  private fun areContentsTheSame(oldItem: RecentsListItem.Episode, newItem: RecentsListItem.Episode) =
    oldItem.episode == newItem.episode &&
    oldItem.season == newItem.season &&
      oldItem.show == newItem.show &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translations == newItem.translations &&
      oldItem.isWatched == newItem.isWatched

  private fun areContentsTheSame(oldItem: RecentsListItem.Header, newItem: RecentsListItem.Header) =
    oldItem == newItem
}
