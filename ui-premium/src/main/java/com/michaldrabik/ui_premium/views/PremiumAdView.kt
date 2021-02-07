package com.michaldrabik.ui_premium.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.michaldrabik.ui_premium.R

class PremiumAdView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_premium_ad, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }
}
