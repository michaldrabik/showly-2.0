package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import androidx.recyclerview.widget.DiffUtil

class MyShowsListItemDiffCallback(
  private val oldList: List<MyShowsListItem>,
  private val newList: List<MyShowsListItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].show.id == newList[newItemPosition].show.id
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (show, _, isLoading) = oldList[oldItemPosition]
    val (show2, _, isLoading2) = newList[newItemPosition]

    return show.id == show2.id && isLoading == isLoading2
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}