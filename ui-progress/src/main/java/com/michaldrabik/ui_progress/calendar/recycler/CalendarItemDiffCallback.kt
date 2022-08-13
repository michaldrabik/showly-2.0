package com.michaldrabik.ui_progress.calendar.recycler

import androidx.recyclerview.widget.DiffUtil

class CalendarItemDiffCallback : DiffUtil.ItemCallback<CalendarListItem>() {

  override fun areItemsTheSame(oldItem: CalendarListItem, newItem: CalendarListItem): Boolean {
    val areEpisodes = oldItem is CalendarListItem.Episode && newItem is CalendarListItem.Episode
    val areHeaders = oldItem is CalendarListItem.Header && newItem is CalendarListItem.Header
    return when {
      areEpisodes -> areItemsTheSame(
        (oldItem as CalendarListItem.Episode),
        (newItem as CalendarListItem.Episode)
      )
      areHeaders -> areItemsTheSame(
        (oldItem as CalendarListItem.Header),
        (newItem as CalendarListItem.Header)
      )
      else -> false
    }
  }

  private fun areItemsTheSame(oldItem: CalendarListItem.Episode, newItem: CalendarListItem.Episode) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt

  private fun areItemsTheSame(oldItem: CalendarListItem.Header, newItem: CalendarListItem.Header) =
    oldItem.textResId == newItem.textResId

  override fun areContentsTheSame(oldItem: CalendarListItem, newItem: CalendarListItem) =
    when (oldItem) {
      is CalendarListItem.Episode -> areContentsTheSame(oldItem, (newItem as CalendarListItem.Episode))
      is CalendarListItem.Header -> areContentsTheSame(oldItem, (newItem as CalendarListItem.Header))
    }

  private fun areContentsTheSame(oldItem: CalendarListItem.Episode, newItem: CalendarListItem.Episode) =
    oldItem.episode == newItem.episode &&
      oldItem.season == newItem.season &&
      oldItem.show == newItem.show &&
      oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translations == newItem.translations &&
      oldItem.isWatched == newItem.isWatched

  private fun areContentsTheSame(oldItem: CalendarListItem.Header, newItem: CalendarListItem.Header) =
    oldItem == newItem
}
