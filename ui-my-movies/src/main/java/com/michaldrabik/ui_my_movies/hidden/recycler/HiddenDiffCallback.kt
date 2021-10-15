package com.michaldrabik.ui_my_movies.hidden.recycler

import androidx.recyclerview.widget.DiffUtil

class HiddenDiffCallback : DiffUtil.ItemCallback<HiddenListItem>() {

  override fun areItemsTheSame(oldItem: HiddenListItem, newItem: HiddenListItem) =
    oldItem.movie.traktId == newItem.movie.traktId

  override fun areContentsTheSame(oldItem: HiddenListItem, newItem: HiddenListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation &&
      oldItem.userRating == newItem.userRating
}
