package com.michaldrabik.ui_lists.details.recycler

import androidx.recyclerview.widget.DiffUtil

class ListDetailsDiffCallback : DiffUtil.ItemCallback<ListDetailsItem>() {

  override fun areItemsTheSame(oldItem: ListDetailsItem, newItem: ListDetailsItem) =
    oldItem.getId() == newItem.getId()

  override fun areContentsTheSame(oldItem: ListDetailsItem, newItem: ListDetailsItem) = when {
    oldItem.isShow() -> {
      oldItem.show == newItem.show &&
        oldItem.isLoading == newItem.isLoading &&
        oldItem.translation == newItem.translation &&
        oldItem.image == newItem.image &&
        oldItem.listedAt == newItem.listedAt &&
        oldItem.rank == newItem.rank
    }
    oldItem.isMovie() -> {
      oldItem.movie == newItem.movie &&
        oldItem.isLoading == newItem.isLoading &&
        oldItem.translation == newItem.translation &&
        oldItem.image == newItem.image &&
        oldItem.listedAt == newItem.listedAt &&
        oldItem.rank == newItem.rank
    }
    else -> throw IllegalStateException()
  }
}
