package com.michaldrabik.showly2.ui.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback : DiffUtil.ItemCallback<WatchlistItem>() {

  override fun areItemsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem) =
    oldItem.show.id == newItem.show.id && oldItem.isHeader() == newItem.isHeader()

  override fun areContentsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem) =
    oldItem.episode.id == newItem.episode.id
        && oldItem.episodesCount == newItem.episodesCount
        && oldItem.watchedEpisodesCount == newItem.watchedEpisodesCount
}