package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import kotlinx.android.synthetic.main.view_add_to_shows_button.view.*

class AddToShowsButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  companion object {
    private const val TRANSITION_DURATION = 200L
  }

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)
  }

  private var isWatched = false

  fun setWatched(withAnimation: Boolean = true) {
    if (isWatched) return
    isWatched = true
    val transition = if (!withAnimation) 0 else TRANSITION_DURATION
    addToMyShowsButton.fadeOut(transition)
    inMyShowsButton.fadeIn(transition)
  }

  fun setUnwatched(withAnimation: Boolean = true) {
    if (!isWatched) return
    isWatched = false
    val transition = if (!withAnimation) 0 else TRANSITION_DURATION
    addToMyShowsButton.fadeIn(transition)
    inMyShowsButton.fadeOut(transition)
  }

  override fun setOnClickListener(l: OnClickListener?) {
    addToMyShowsButton.setOnClickListener(l)
    inMyShowsButton.setOnClickListener(l)
  }
}