package com.michaldrabik.showly2.ui.common.views.search

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.widget.LinearLayout
import com.michaldrabik.showly2.R

class EmptySearchView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_search_empty, this)
    orientation = VERTICAL
    gravity = CENTER
  }
}
