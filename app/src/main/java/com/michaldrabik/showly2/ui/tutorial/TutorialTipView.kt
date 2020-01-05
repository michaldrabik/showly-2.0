package com.michaldrabik.showly2.ui.tutorial

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.onClick

class TutorialTipView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  companion object {
    private const val ANIMATION_DURATION = 5000L
  }

  private val animatorX by lazy { ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.2F, 1F, 0.8F, 1F) }
  private val animatorY by lazy { ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.2F, 1F, 0.8F, 1F) }
  private val animatorSet by lazy {
    AnimatorSet().apply {
      playTogether(animatorX, animatorY)
      duration = ANIMATION_DURATION
    }
  }

  var onClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_tutorial_tip, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    clipChildren = false
    onClick { onClickListener?.invoke() }
    startAnimation()
  }

  private fun startAnimation() {
    animatorSet.doOnEnd { startAnimation() }
    animatorSet.start()
  }
}
