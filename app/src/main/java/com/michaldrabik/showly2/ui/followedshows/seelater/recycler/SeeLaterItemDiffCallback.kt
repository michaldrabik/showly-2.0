package com.michaldrabik.showly2.ui.followedshows.seelater.recycler

import androidx.recyclerview.widget.DiffUtil

class SeeLaterItemDiffCallback : DiffUtil.ItemCallback<SeeLaterListItem>() {

  override fun areItemsTheSame(oldItem: SeeLaterListItem, newItem: SeeLaterListItem) =
    oldItem.show.id == newItem.show.id

  override fun areContentsTheSame(oldItem: SeeLaterListItem, newItem: SeeLaterListItem) =
    oldItem.image == newItem.image && oldItem.isLoading == newItem.isLoading
}