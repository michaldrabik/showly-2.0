package com.michaldrabik.showly2.ui.search.recycler

import androidx.recyclerview.widget.DiffUtil

class SearchItemDiffCallback : DiffUtil.ItemCallback<SearchListItem>() {

  override fun areItemsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.show.id == newItem.show.id

  override fun areContentsTheSame(oldItem: SearchListItem, newItem: SearchListItem) =
    oldItem.image == newItem.image && oldItem.isLoading == newItem.isLoading
}