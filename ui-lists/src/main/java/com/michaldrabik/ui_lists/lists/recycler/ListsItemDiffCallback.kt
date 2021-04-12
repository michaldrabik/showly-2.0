package com.michaldrabik.ui_lists.lists.recycler

import androidx.recyclerview.widget.DiffUtil

class ListsItemDiffCallback : DiffUtil.ItemCallback<ListsItem>() {

  override fun areItemsTheSame(oldItem: ListsItem, newItem: ListsItem) =
    oldItem.list.id == newItem.list.id

  override fun areContentsTheSame(oldItem: ListsItem, newItem: ListsItem) =
    oldItem.list == newItem.list &&
      oldItem.sortOrder == newItem.sortOrder &&
      oldItem.images.size == newItem.images.size &&
      oldItem.images.toTypedArray().contentDeepEquals(newItem.images.toTypedArray()) &&
      oldItem.dateFormat?.toString() == newItem.dateFormat?.toString()
}
