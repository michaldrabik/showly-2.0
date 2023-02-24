package com.michaldrabik.ui_lists.details.recycler

import androidx.recyclerview.widget.DiffUtil

class ListDetailsDiffCallback(
  private val oldItems: List<ListDetailsItem>,
  private val newItems: List<ListDetailsItem>,
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
    if (oldItems[oldPos].isMovie() && newItems[newPos].isShow()) return false
    if (oldItems[oldPos].isShow() && newItems[newPos].isMovie()) return false
    return oldItems[oldPos].id == newItems[newPos].id
  }

  override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
    val oldItem = oldItems[oldPos]
    val newItem = newItems[newPos]
    return when {
      oldItem.isShow() -> {
        oldItem.show == newItem.show &&
          oldItem.isLoading == newItem.isLoading &&
          oldItem.isRankDisplayed == newItem.isRankDisplayed &&
          oldItem.isEnabled == newItem.isEnabled &&
          oldItem.isWatchlist == newItem.isWatchlist &&
          oldItem.isWatched == newItem.isWatched &&
          oldItem.isManageMode == newItem.isManageMode &&
          oldItem.translation == newItem.translation &&
          oldItem.userRating == newItem.userRating &&
          oldItem.image == newItem.image &&
          oldItem.listedAt == newItem.listedAt &&
          oldItem.sortOrder == newItem.sortOrder &&
          oldItem.rankDisplay == newItem.rankDisplay &&
          oldItem.rank == newItem.rank
      }
      oldItem.isMovie() -> {
        oldItem.movie == newItem.movie &&
          oldItem.isLoading == newItem.isLoading &&
          oldItem.isRankDisplayed == newItem.isRankDisplayed &&
          oldItem.isManageMode == newItem.isManageMode &&
          oldItem.isWatchlist == newItem.isWatchlist &&
          oldItem.isWatched == newItem.isWatched &&
          oldItem.isEnabled == newItem.isEnabled &&
          oldItem.translation == newItem.translation &&
          oldItem.userRating == newItem.userRating &&
          oldItem.image == newItem.image &&
          oldItem.listedAt == newItem.listedAt &&
          oldItem.sortOrder == newItem.sortOrder &&
          oldItem.rankDisplay == newItem.rankDisplay &&
          oldItem.rank == newItem.rank
      }
      else -> throw IllegalStateException()
    }
  }

  override fun getOldListSize() = oldItems.size

  override fun getNewListSize() = newItems.size
}
