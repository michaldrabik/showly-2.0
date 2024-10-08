package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewRateValueBinding

class RateValueView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewRateValueBinding.inflate(LayoutInflater.from(context), this)

  private val translation by lazy { resources.getDimensionPixelSize(R.dimen.rateValueTranslation).toFloat() }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  fun setValue(value: String) {
    with(binding) {
      viewRateValueText1.text = value
      viewRateValueText1.alpha = 1F
      viewRateValueText2.text = ""
      viewRateValueText2.alpha = 0F
    }
  }

  fun setValueAnimated(
    value: String,
    direction: Direction = Direction.LEFT,
  ) {
    val translation = when (direction) {
      Direction.LEFT -> -translation
      Direction.RIGHT -> translation
    }
    with(binding) {
      viewRateValueText2.alpha = 0F
      viewRateValueText2.text = value
      viewRateValueText2.translationX = -translation
      viewRateValueText2
        .animate()
        .translationX(0F)
        .alpha(1F)
        .setDuration(175L)
        .start()

      viewRateValueText1
        .animate()
        .translationX(translation)
        .setDuration(175L)
        .alpha(0F)
        .withEndAction {
          viewRateValueText2.alpha = 0F
          viewRateValueText1.translationX = 0F
          viewRateValueText1.alpha = 1F
          viewRateValueText1.text = value
        }.start()
    }
  }

  enum class Direction {
    LEFT,
    RIGHT,
  }
}
