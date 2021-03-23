package com.michaldrabik.ui_lists.details.helpers

interface ReorderListCallbackAdapter {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
  fun onItemMoveFinished()
}
