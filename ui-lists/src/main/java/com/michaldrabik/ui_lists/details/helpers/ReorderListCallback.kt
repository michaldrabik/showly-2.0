package com.michaldrabik.ui_lists.details.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView

class ReorderListCallback(
  private val adapter: ReorderListCallbackAdapter
) : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, START) {

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    adapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    adapter.onItemSwiped(viewHolder)
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    super.clearView(recyclerView, viewHolder)
    adapter.onItemCleared()
  }

  override fun isItemViewSwipeEnabled() = false

  override fun isLongPressDragEnabled() = false
}
