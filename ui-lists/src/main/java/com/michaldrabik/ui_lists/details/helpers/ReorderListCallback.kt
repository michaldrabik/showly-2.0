package com.michaldrabik.ui_lists.details.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView

class ReorderListCallback(
  private val adapter: ReorderListCallbackAdapter
) : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    return true
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    super.clearView(recyclerView, viewHolder)
    adapter.onItemMoveFinished()
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
}
