package com.michaldrabik.showly2.ui.show.seasons.episodes

import androidx.recyclerview.widget.DiffUtil

class EpisodeListItemDiffCallback(
  private val oldList: List<EpisodeListItem>,
  private val newList: List<EpisodeListItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
    oldList[oldItemPosition].id == newList[newItemPosition].id

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (_, isWatch) = oldList[oldItemPosition]
    val (_, isWatch2) = newList[newItemPosition]

    return isWatch == isWatch2
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}