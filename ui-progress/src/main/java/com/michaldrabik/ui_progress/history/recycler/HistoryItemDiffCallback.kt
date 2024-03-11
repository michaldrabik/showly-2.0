package com.michaldrabik.ui_progress.history.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_progress.history.entities.HistoryListItem

internal class HistoryItemDiffCallback : DiffUtil.ItemCallback<HistoryListItem>() {

  override fun areItemsTheSame(
    oldItem: HistoryListItem,
    newItem: HistoryListItem
  ): Boolean {
    val areEpisodes = oldItem is HistoryListItem.Episode && newItem is HistoryListItem.Episode
    val areHeaders = oldItem is HistoryListItem.Header && newItem is HistoryListItem.Header
    val areFilters = oldItem is HistoryListItem.Filters && newItem is HistoryListItem.Filters
    return when {
      areEpisodes -> areItemsTheSame(
        (oldItem as HistoryListItem.Episode),
        (newItem as HistoryListItem.Episode)
      )
      areHeaders -> areItemsTheSame(
        (oldItem as HistoryListItem.Header),
        (newItem as HistoryListItem.Header)
      )
      areFilters -> true
      else -> false
    }
  }

  private fun areItemsTheSame(
    oldItem: HistoryListItem.Episode,
    newItem: HistoryListItem.Episode
  ): Boolean {
    return oldItem.episode.ids.trakt == newItem.episode.ids.trakt
  }

  private fun areItemsTheSame(
    oldItem: HistoryListItem.Header,
    newItem: HistoryListItem.Header
  ): Boolean {
    return oldItem.date == newItem.date
  }

  override fun areContentsTheSame(
    oldItem: HistoryListItem,
    newItem: HistoryListItem
  ): Boolean {
    return when (oldItem) {
      is HistoryListItem.Episode -> areContentsTheSame(oldItem, (newItem as HistoryListItem.Episode))
      is HistoryListItem.Header -> areContentsTheSame(oldItem, (newItem as HistoryListItem.Header))
      is HistoryListItem.Filters -> areContentsTheSame(oldItem, (newItem as HistoryListItem.Filters))
    }
  }

  private fun areContentsTheSame(
    oldItem: HistoryListItem.Episode,
    newItem: HistoryListItem.Episode
  ): Boolean {
    return oldItem.episode == newItem.episode &&
      oldItem.season == newItem.season &&
      oldItem.show == newItem.show &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translations == newItem.translations
  }

  private fun areContentsTheSame(
    oldItem: HistoryListItem.Header,
    newItem: HistoryListItem.Header
  ): Boolean {
    return oldItem == newItem
  }

  private fun areContentsTheSame(
    oldItem: HistoryListItem.Filters,
    newItem: HistoryListItem.Filters,
  ): Boolean {
    return oldItem.period == newItem.period
  }
}
