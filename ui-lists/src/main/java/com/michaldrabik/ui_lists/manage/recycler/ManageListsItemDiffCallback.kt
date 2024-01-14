package com.michaldrabik.ui_lists.manage.recycler

import androidx.recyclerview.widget.DiffUtil

class ManageListsItemDiffCallback : DiffUtil.ItemCallback<ManageListsItem>() {

  override fun areItemsTheSame(oldItem: ManageListsItem, newItem: ManageListsItem) =
    oldItem.list.id == newItem.list.id

  override fun areContentsTheSame(oldItem: ManageListsItem, newItem: ManageListsItem) =
    oldItem.list == newItem.list &&
      oldItem.isChecked == newItem.isChecked &&
      oldItem.isEnabled == newItem.isEnabled
}
