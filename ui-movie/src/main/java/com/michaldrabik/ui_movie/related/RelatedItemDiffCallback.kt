package com.michaldrabik.ui_movie.related

import androidx.recyclerview.widget.DiffUtil

class RelatedItemDiffCallback : DiffUtil.ItemCallback<RelatedListItem>() {

  override fun areItemsTheSame(oldItem: RelatedListItem, newItem: RelatedListItem) =
    oldItem.movie.ids.trakt == newItem.movie.ids.trakt

  override fun areContentsTheSame(oldItem: RelatedListItem, newItem: RelatedListItem) =
    oldItem.image == newItem.image && oldItem.isLoading == newItem.isLoading
}
