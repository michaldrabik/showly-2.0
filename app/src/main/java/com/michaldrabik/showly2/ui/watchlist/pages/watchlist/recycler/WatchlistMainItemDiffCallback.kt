package com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistMainItemDiffCallback : DiffUtil.ItemCallback<WatchlistMainItem>() {

  override fun areItemsTheSame(oldItem: WatchlistMainItem, newItem: WatchlistMainItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt && oldItem.isHeader() == newItem.isHeader()

  override fun areContentsTheSame(oldItem: WatchlistMainItem, newItem: WatchlistMainItem) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt &&
      oldItem.episodesCount == newItem.episodesCount &&
      oldItem.watchedEpisodesCount == newItem.watchedEpisodesCount &&
      oldItem.season == newItem.season &&
      oldItem.season.episodes == newItem.season.episodes &&
      oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image
}
