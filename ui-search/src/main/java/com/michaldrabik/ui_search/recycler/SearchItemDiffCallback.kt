package com.michaldrabik.ui_search.recycler

import androidx.recyclerview.widget.DiffUtil

class SearchItemDiffCallback : DiffUtil.ItemCallback<SearchListItem>() {

  override fun areItemsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.image == newItem.image &&
      oldItem.isFollowed == newItem.isFollowed &&
      oldItem.isWatchlist == newItem.isWatchlist &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation
}
