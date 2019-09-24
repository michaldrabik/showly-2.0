package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import kotlinx.android.synthetic.main.view_add_to_shows_button.view.*

class AddToShowsButton @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    private const val TRANSITION_DURATION = 200L
  }

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)
  }

  private var isWatched = false

  fun setWatched() {
    if (isWatched) return
    isWatched = true
    addToMyShowsButton.fadeOut(TRANSITION_DURATION)
    inMyShowsButton.fadeIn(TRANSITION_DURATION)
  }

  fun setUnwatched() {
    if (!isWatched) return
    isWatched = false
    addToMyShowsButton.fadeIn(TRANSITION_DURATION)
    inMyShowsButton.fadeOut(TRANSITION_DURATION)
  }

  override fun setOnClickListener(l: OnClickListener?) {
    addToMyShowsButton.setOnClickListener(l)
    inMyShowsButton.setOnClickListener(l)
  }
}