package com.michaldrabik.ui_my_shows.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback : DiffUtil.ItemCallback<WatchlistListItem>() {

  override fun areItemsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: WatchlistListItem, newItem: WatchlistListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
}
