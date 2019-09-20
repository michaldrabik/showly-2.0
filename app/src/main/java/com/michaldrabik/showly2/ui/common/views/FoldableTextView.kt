package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class FoldableTextView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

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