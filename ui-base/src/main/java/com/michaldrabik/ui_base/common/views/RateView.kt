package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_rate.view.*

class RateView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_rate, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = VERTICAL
  }

  private val stars = listOf<ImageView>(star1, star2, star3, star4, star5, star6, star7, star8, star9, star10)
  private var rating = INITIAL_RATING

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    stars.forEach { star -> star.onClick { setRating(it.tag.toString().toInt()) } }
  }

  fun setRating(rate: Int) {
    rating = rate.coerceIn(1..10)
    stars.forEach { it.setImageResource(R.drawable.ic_star_empty) }
    (1..rating).forEachIndexed { index, _ ->
      stars[index].setImageResource(R.drawable.ic_star)
    }
    viewRateText.text = rating.toString()
  }

  fun getRating() = rating
}
