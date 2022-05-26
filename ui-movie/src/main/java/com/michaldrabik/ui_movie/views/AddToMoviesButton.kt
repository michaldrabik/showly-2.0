package com.michaldrabik.ui_movie.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.ADD
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_MY_MOVIES
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_WATCHLIST
import kotlinx.android.synthetic.main.view_add_to_movies_button.view.*

class AddToMoviesButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onAddMyMoviesClickListener: (() -> Unit)? = null
  var onAddWatchLaterClickListener: (() -> Unit)? = null
  var onRemoveClickListener: (() -> Unit)? = null

  private var state: State = ADD
  private var isAnimating = false

  init {
    inflate(context, R.layout.view_add_to_movies_button, this)
    addToMyMoviesButton.onClick { if (!isAnimating) onAddMyMoviesClickListener?.invoke() }
    watchlistButton.onClick { if (!isAnimating) onAddWatchLaterClickListener?.invoke() }
    addedToButton.onClick { if (!isAnimating) onRemoveClickListener?.invoke() }
    checkButton.onClick { if (!isAnimating) onAddMyMoviesClickListener?.invoke() }
  }

  fun setState(state: State, animate: Boolean = false) {
    if (state == this.state) return
    this.state = state

    val duration = if (animate) 175L else 0
    val startDelay = if (animate) 200L else 0
    if (animate) isAnimating = true

    when (state) {
      ADD -> {
        addToMyMoviesButton.setText(R.string.textAddToMyMovies)
        checkButton.fadeOut(duration, withHardware = true)
        addedToButton.fadeOut(duration, withHardware = true)
        addToMyMoviesButton.fadeIn(duration, startDelay = startDelay, withHardware = true)
        watchlistButton.fadeIn(duration, startDelay = startDelay, withHardware = true) { isAnimating = false }
      }
      IN_MY_MOVIES -> {
        val color = context.colorFromAttr(R.attr.colorAccent)
        val colorState = context.colorStateListFromAttr(R.attr.colorAccent)

        addToMyMoviesButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        checkButton.fadeOut(duration, withHardware = true)
        addedToButton.fadeOut(duration, withHardware = true) {
          addedToButton.run {
            setIconResource(R.drawable.ic_bookmark_full)
            setText(R.string.textInMyMovies)
            setTextColor(color)
            iconTint = colorState
            strokeColor = colorState
            rippleColor = colorState
            fadeIn(duration, withHardware = true) { isAnimating = false }
          }
        }
      }
      IN_WATCHLIST -> {
        val color = context.colorFromAttr(android.R.attr.textColorSecondary)
        val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)

        addToMyMoviesButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        checkButton.fadeIn(duration, startDelay = startDelay, withHardware = true)
        addedToButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInMoviesWatchlist)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, startDelay = startDelay, withHardware = true) { isAnimating = false }
        }
      }
      State.IN_HIDDEN -> {
        val delay = if (addToMyMoviesButton.isVisible) startDelay else 0
        addToMyMoviesButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        checkButton.fadeOut(duration, withHardware = true)
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

  override fun setEnabled(enabled: Boolean) {
    addToMyMoviesButton.isEnabled = enabled
    addToMyMoviesButton.isClickable = enabled
    watchlistButton.isEnabled = enabled
    watchlistButton.isClickable = enabled
    addedToButton.isEnabled = enabled
    addedToButton.isClickable = enabled
    checkButton.isEnabled = enabled
    checkButton.isClickable = enabled
  }

  enum class State {
    ADD,
    IN_MY_MOVIES,
    IN_WATCHLIST,
    IN_HIDDEN
  }
}
