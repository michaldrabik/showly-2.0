package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FoldableTextView : AppCompatTextView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  companion object {
    private const val MIN_LINES = 3
    private const val MAX_LINES = 100
  }

  private var initialLines = MIN_LINES

  init {
    maxLines = initialLines
    setOnClickListener {
      maxLines = if (maxLines == MAX_LINES) initialLines else MAX_LINES
    }
  }

  fun setInitialLines(lines: Int) {
    initialLines = lines
    maxLines = lines
  }
}
