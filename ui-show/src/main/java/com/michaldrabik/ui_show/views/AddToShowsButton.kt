package com.michaldrabik.ui_show.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_show.R
import kotlinx.android.synthetic.main.view_add_to_shows_button.view.*

class AddToShowsButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onAddMyShowsClickListener: (() -> Unit)? = null
  var onAddWatchLaterClickListener: (() -> Unit)? = null
  var onRemoveClickListener: (() -> Unit)? = null

  private var state: State = State.ADD

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)

    addToMyShowsButton.onClick { onAddMyShowsClickListener?.invoke() }
    watchlistButton.onClick { onAddWatchLaterClickListener?.invoke() }
    addedToButton.onClick { onRemoveClickListener?.invoke() }
  }

  fun setState(state: State, animate: Boolean = false) {
    this.state = state
    val duration = if (animate) 200L else 0
    if (animate) isEnabled = false
    when (state) {
      State.ADD -> {
        addToMyShowsButton.fadeIn(duration, withHardware = true)
        watchlistButton.fadeIn(duration, withHardware = true)
        addedToButton.fadeOut(duration, withHardware = true) { isEnabled = true }
      }
      State.IN_MY_SHOWS -> {
        val color = context.colorFromAttr(R.attr.colorAccent)
        val colorState = context.colorStateListFromAttr(R.attr.colorAccent)

        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        addedToButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInMyShows)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
      }
      State.IN_WATCHLIST -> {
        val color = context.colorFromAttr(android.R.attr.textColorSecondary)
        val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)

        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        addedToButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInWatchlist)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
      }
      State.IN_HIDDEN -> {
        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        addedToButton.run {
          val color = context.colorFromAttr(android.R.attr.textColorSecondary)
          val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)
          setIconResource(R.drawable.ic_eye_no)
          setText(R.string.textInHidden)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    addToMyShowsButton.isEnabled = enabled
    watchlistButton.isEnabled = enabled
    addedToButton.isEnabled = enabled
  }

  enum class State {
    ADD,
    IN_MY_SHOWS,
    IN_WATCHLIST,
    IN_HIDDEN
  }
}
