package com.michaldrabik.showly2.ui.watchlist.recycler

import androidx.recyclerview.widget.DiffUtil

class WatchlistItemDiffCallback(
  private val oldList: List<WatchlistItem>,
  private val newList: List<WatchlistItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
    oldList[oldItemPosition].show.id == newList[newItemPosition].show.id
        && oldList[oldItemPosition].isHeader() == newList[newItemPosition].isHeader()

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val (_, episode1, episodesCount1, watchedEpisodes1) = oldList[oldItemPosition]
    val (_, episode2, episodesCount2, watchedEpisodes2) = newList[newItemPosition]

    return episode1.id == episode2.id
        && episodesCount1 == episodesCount2
        && watchedEpisodes1 == watchedEpisodes2
  }

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size
}