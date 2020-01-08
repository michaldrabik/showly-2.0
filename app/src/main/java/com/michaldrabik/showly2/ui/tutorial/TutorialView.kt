package com.michaldrabik.showly2.ui.tutorial

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_tutorial_view.view.*

class TutorialView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_tutorial_view, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setBackgroundResource(R.color.colorMask)
    setupView()
  }

  var onOkClick: (() -> Unit)? = null

  private fun setupView() {
    onClick { /* Block background clicks */ }
    tutorialViewButton.onClick { onOkClick?.invoke() }
  }

  fun showTip(@StringRes tipStringRes: Int) {
    tutorialViewText.setText(tipStringRes)
    tutorialTipView.fadeIn()
  }

  fun hideTip() {
    tutorialViewText.text = ""
    tutorialTipView.fadeOut()
  }
}
