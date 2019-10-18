package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class FoldableTextView : TextView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  companion object {
    private const val MIN_LINES = 3
    private const val MAX_LINES = 100
  }

  init {
    maxLines = MIN_LINES
    setOnClickListener {
      maxLines = if (maxLines == MAX_LINES) MIN_LINES else MAX_LINES
    }
  }
}