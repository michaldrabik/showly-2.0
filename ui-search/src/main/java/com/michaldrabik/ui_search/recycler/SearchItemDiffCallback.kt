package com.michaldrabik.ui_search.recycler

import androidx.recyclerview.widget.DiffUtil

class SearchItemDiffCallback : DiffUtil.ItemCallback<SearchListItem>() {

  override fun areItemsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.image == newItem.image &&
      oldItem.isFollowed == newItem.isFollowed &&
      oldItem.isSeeLater == newItem.isSeeLater &&
      oldItem.isLoading == newItem.isLoading
}
