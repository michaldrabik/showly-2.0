package com.michaldrabik.showly2.ui.tutorial

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.screenHeight
import kotlinx.android.synthetic.main.view_tip_overlay.view.*

class TipOverlayView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_tip_overlay, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setBackgroundResource(R.color.colorMask)
    setupView()
  }

  var onOkClick: (() -> Unit)? = null

  private fun setupView() {
    onClick { /* Block background clicks */ }
    tutorialViewButton.onClick { onOkClick?.invoke() }
  }

  fun showTip(tip: Tip) {
    tutorialViewText.setText(tip.textResId)
    SpringAnimation(tutorialTipView, DynamicAnimation.TRANSLATION_Y, 0F).apply {
      spring.stiffness = 300F
      spring.dampingRatio = 0.65F
      setStartValue((screenHeight().toFloat()) / 3F)
      start()
    }
  }
}
