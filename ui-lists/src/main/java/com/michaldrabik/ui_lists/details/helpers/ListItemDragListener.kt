package com.michaldrabik.ui_lists.details.helpers

import androidx.recyclerview.widget.RecyclerView

interface ListItemDragListener {
  fun onListItemDragStarted(viewHolder: RecyclerView.ViewHolder)
}
