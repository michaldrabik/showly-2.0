package com.michaldrabik.ui_comments.utilities

import android.widget.TextView

fun TextView.refreshTextSelection() {
  setTextIsSelectable(false)
  post { setTextIsSelectable(true) }
}
