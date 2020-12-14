package com.michaldrabik.ui_discover.recycler

import androidx.recyclerview.widget.DiffUtil

class DiscoverItemDiffCallback : DiffUtil.ItemCallback<DiscoverListItem>() {

  override fun areItemsTheSame(oldItem: DiscoverListItem, newItem: DiscoverListItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: DiscoverListItem, newItem: DiscoverListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.isFollowed == newItem.isFollowed &&
      oldItem.isWatchlist == newItem.isWatchlist &&
      oldItem.translation == newItem.translation
}
