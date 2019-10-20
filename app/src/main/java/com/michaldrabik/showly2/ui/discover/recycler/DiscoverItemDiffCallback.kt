package com.michaldrabik.showly2.ui.discover.recycler

import androidx.recyclerview.widget.DiffUtil

class DiscoverItemDiffCallback(
  private val oldList: List<DiscoverListItem>,
  private val newList: List<DiscoverListItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
    oldList[oldItemPosition].show.id == newList[newItemPosition].show.id

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (_, image1, isLoading1) = oldList[oldItemPosition]
    val (_, image2, isLoading2) = newList[newItemPosition]

    return image1 == image2 && isLoading1 == isLoading2
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}