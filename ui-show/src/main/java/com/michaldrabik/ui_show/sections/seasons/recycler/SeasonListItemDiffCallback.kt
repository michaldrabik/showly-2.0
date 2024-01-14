package com.michaldrabik.ui_show.sections.seasons.recycler

import androidx.recyclerview.widget.DiffUtil

class SeasonListItemDiffCallback(
  private val oldList: List<SeasonListItem>,
  private val newList: List<SeasonListItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].id == newList[newItemPosition].id
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (_, _, episodes, isWatched) = oldList[oldItemPosition]
    val (_, _, episodes2, isWatched2) = newList[newItemPosition]

    if (episodes.size != episodes2.size) return isWatched == isWatched2

    var areEpisodesTheSame = true
    episodes.forEach { e1 ->
      val e2 = episodes2.firstOrNull { it.id == e1.id }
      e2?.let {
        if (e1.isWatched != e2.isWatched || e1.myRating != e2.myRating) {
          areEpisodesTheSame = false
          return@forEach
        }
      }
    }

    return isWatched == isWatched2 && areEpisodesTheSame
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}
