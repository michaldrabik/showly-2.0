package com.michaldrabik.showly2.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_rate_app.view.*

class RateAppView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onYesClickListener: (() -> Unit)? = null
  var onNoClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_rate_app, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewRateAppYesButton.onClick { onYesClickListener?.invoke() }
    viewRateAppNoButton.onClick { onNoClickListener?.invoke() }
  }
}
