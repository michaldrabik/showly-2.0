package com.michaldrabik.ui_discover_movies.recycler

import androidx.recyclerview.widget.DiffUtil

class DiscoverMovieItemDiffCallback : DiffUtil.ItemCallback<DiscoverMovieListItem>() {

  override fun areItemsTheSame(oldItem: DiscoverMovieListItem, newItem: DiscoverMovieListItem) =
    oldItem.movie.ids.trakt == newItem.movie.ids.trakt

  override fun areContentsTheSame(oldItem: DiscoverMovieListItem, newItem: DiscoverMovieListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.isCollected == newItem.isCollected &&
      oldItem.isWatchlist == newItem.isWatchlist &&
      oldItem.translation == newItem.translation
}
