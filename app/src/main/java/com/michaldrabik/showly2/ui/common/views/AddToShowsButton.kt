package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_add_to_shows_button.view.*

class AddToShowsButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)
  }

  private var isWatched = false

  fun setWatched() {
    if (isWatched) return
    isWatched = true
    addToMyShowsButton.gone()
    inMyShowsButton.visible()
  }

  fun setUnwatched() {
    if (!isWatched) return
    isWatched = false
    addToMyShowsButton.visible()
    inMyShowsButton.gone()
  }

  override fun setOnClickListener(l: OnClickListener?) {
    addToMyShowsButton.setOnClickListener(l)
    inMyShowsButton.setOnClickListener(l)
  }
}