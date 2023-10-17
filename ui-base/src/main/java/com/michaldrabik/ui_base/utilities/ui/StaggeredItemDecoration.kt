package com.michaldrabik.ui_base.utilities.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class StaggeredItemDecoration(private val offset: Int) : ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State,
  ) {
    val position = parent.getChildAdapterPosition(view)
    val halfOffset = offset / 2

    if (position.mod(2) == 0) {
      outRect.right = halfOffset
    } else {
      outRect.left = halfOffset
    }
    outRect.bottom = offset
  }
}
