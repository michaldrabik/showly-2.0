package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import androidx.recyclerview.widget.DiffUtil

class MyShowsListItemDiffCallback : DiffUtil.ItemCallback<MyShowsListItem>() {

  override fun areItemsTheSame(oldItem: MyShowsListItem, newItem: MyShowsListItem) =
    oldItem.show.id == newItem.show.id

  override fun areContentsTheSame(oldItem: MyShowsListItem, newItem: MyShowsListItem) =
    oldItem.image == newItem.image && oldItem.isLoading == newItem.isLoading
}