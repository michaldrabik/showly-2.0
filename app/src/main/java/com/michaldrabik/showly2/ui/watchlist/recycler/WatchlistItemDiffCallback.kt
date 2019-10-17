package com.michaldrabik.showly2.ui.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback(
  private val oldList: List<WatchlistItem>,
  private val newList: List<WatchlistItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
    oldList[oldItemPosition].show.id == newList[newItemPosition].show.id

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (show1, episode1) = oldList[oldItemPosition]
    val (show2, episode2) = newList[newItemPosition]

    return episode1.id == episode2.id && show1.id == show2.id
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}