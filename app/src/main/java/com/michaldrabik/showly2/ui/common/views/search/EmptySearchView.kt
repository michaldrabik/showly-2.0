package com.michaldrabik.showly2.ui.common.views.search

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.widget.LinearLayout
import com.michaldrabik.showly2.R

class EmptySearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_search_empty, this)
    orientation = VERTICAL
    gravity = CENTER
  }
}