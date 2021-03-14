package com.michaldrabik.ui_lists

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout

@SuppressLint("SetTextI18n")
class ListsTripleImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_triple_image, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }
}
