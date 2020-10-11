package com.michaldrabik.ui_watchlist

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback : DiffUtil.ItemCallback<WatchlistItem>() {

  override fun areItemsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt && oldItem.isHeader() == newItem.isHeader()

  override fun areContentsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem) =
    oldItem.episode.ids.trakt == newItem.episode.ids.trakt &&
      oldItem.upcomingEpisode.ids.trakt == newItem.upcomingEpisode.ids.trakt &&
      oldItem.episodesCount == newItem.episodesCount &&
      oldItem.watchedEpisodesCount == newItem.watchedEpisodesCount &&
      oldItem.season == newItem.season &&
      oldItem.season.episodes == newItem.season.episodes &&
      oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image
}
