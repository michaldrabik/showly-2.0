package com.michaldrabik.ui_movie.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
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

  init {
    inflate(context, R.layout.view_add_to_movies_button, this)

    addToMyMoviesButton.onClick { onAddMyMoviesClickListener?.invoke() }
    watchlistButton.onClick { onAddWatchLaterClickListener?.invoke() }
    inMyMoviesButton.onClick { onRemoveClickListener?.invoke() }
  }

  fun setState(state: State, animate: Boolean = false) {
    this.state = state
    val duration = if (animate) 250L else 0
    if (animate) isEnabled = false
    when (state) {
      ADD -> {
        addToMyMoviesButton.fadeIn(duration)
        watchlistButton.fadeIn(duration)
        inMyMoviesButton.fadeOut(duration) { isEnabled = true }
      }
      IN_MY_MOVIES -> {
        val color = context.colorFromAttr(R.attr.colorAccent)
        val colorState = context.colorStateListFromAttr(R.attr.colorAccent)

        addToMyMoviesButton.fadeOut(duration)
        watchlistButton.fadeOut(duration)
        inMyMoviesButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInMyMovies)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration) { isEnabled = true }
        }
      }
      IN_WATCHLIST -> {
        val color = context.colorFromAttr(android.R.attr.textColorSecondary)
        val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)

        addToMyMoviesButton.fadeOut(duration)
        watchlistButton.fadeOut(duration)
        inMyMoviesButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInMoviesWatchlist)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration) { isEnabled = true }
        }
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    addToMyMoviesButton.isEnabled = enabled
    watchlistButton.isEnabled = enabled
    inMyMoviesButton.isEnabled = enabled
  }

  enum class State {
    ADD,
    IN_MY_MOVIES,
    IN_WATCHLIST
  }
}
