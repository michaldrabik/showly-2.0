package com.michaldrabik.ui_show.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.ViewAddToShowsButtonBinding

class AddToShowsButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewAddToShowsButtonBinding.inflate(LayoutInflater.from(context), this)

  var onAddMyShowsClickListener: (() -> Unit)? = null
  var onAddWatchlistClickListener: (() -> Unit)? = null
  var onRemoveClickListener: (() -> Unit)? = null

  private var state: State = State.ADD
  private var isAnimating = false

  init {
    with(binding) {
      addToMyShowsButton.onClick {
        if (!isAnimating) onAddMyShowsClickListener?.invoke()
      }
      watchlistButton.onClick {
        if (!isAnimating) onAddWatchlistClickListener?.invoke()
      }
      addedToButton.onClick {
        if (!isAnimating) onRemoveClickListener?.invoke()
      }
    }
  }

  fun setState(state: State, animate: Boolean = false) {
    if (state == this.state) return
    this.state = state

    val duration = if (animate) 175L else 0
    val startDelay = if (animate) 200L else 0
    if (animate) isAnimating = true

    with(binding) {
      when (state) {
        State.ADD -> {
          addedToButton.fadeOut(duration, withHardware = true)
          addToMyShowsButton.fadeIn(duration, startDelay = startDelay, withHardware = true)
          watchlistButton.fadeIn(duration, startDelay = startDelay, withHardware = true) { isAnimating = false }
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
            fadeIn(duration, startDelay = startDelay, withHardware = true) { isAnimating = false }
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
            fadeIn(duration, startDelay = startDelay, withHardware = true) { isAnimating = false }
          }
        }
        State.IN_HIDDEN -> {
          val delay = if (addToMyShowsButton.isVisible) startDelay else 0
          addToMyShowsButton.fadeOut(duration, withHardware = true)
          watchlistButton.fadeOut(duration, withHardware = true)
          with(addedToButton) {
            fadeOut(duration, withHardware = true) {
              val color = context.colorFromAttr(android.R.attr.textColorSecondary)
              val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)
              setIconResource(R.drawable.ic_eye_no)
              setText(R.string.textInHidden)
              setTextColor(color)
              iconTint = colorState
              strokeColor = colorState
              rippleColor = colorState
              fadeIn(duration, startDelay = delay, withHardware = true) { isAnimating = false }
            }
          }
        }
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    with(binding) {
      addToMyShowsButton.isEnabled = enabled
      addToMyShowsButton.isClickable = enabled
      watchlistButton.isEnabled = enabled
      watchlistButton.isClickable = enabled
      addedToButton.isEnabled = enabled
      addedToButton.isClickable = enabled
    }
  }

  enum class State {
    ADD,
    IN_MY_SHOWS,
    IN_WATCHLIST,
    IN_HIDDEN
  }
}
