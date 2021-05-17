package com.michaldrabik.ui_show.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
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
  var onArchiveClickListener: (() -> Unit)? = null

  private var state: State = State.ADD

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)

    addToMyShowsButton.onClick { onAddMyShowsClickListener?.invoke() }
    watchlistButton.onClick { onAddWatchLaterClickListener?.invoke() }
    inMyShowsButton.onClick { onRemoveClickListener?.invoke() }
    archiveButton.onClick { onArchiveClickListener?.invoke() }
  }

  fun setState(state: State, animate: Boolean = false) {
    this.state = state
    val duration = if (animate) 200L else 0
    if (animate) isEnabled = false
    when (state) {
      State.ADD -> {
        addToMyShowsButton.fadeIn(duration, withHardware = true)
        watchlistButton.fadeIn(duration, withHardware = true)
        inMyShowsButton.fadeOut(duration, withHardware = true) { isEnabled = true }
        archiveButton.fadeOut(duration, withHardware = true) { isEnabled = true }
      }
      State.IN_MY_SHOWS -> {
        val color = context.colorFromAttr(R.attr.colorAccent)
        val colorState = context.colorStateListFromAttr(R.attr.colorAccent)

        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        inMyShowsButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInMyShows)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
        archiveButton.run {
          fadeIn(duration, withHardware = true) { isEnabled = true }
          setColorFilter(color)
        }
      }
      State.IN_WATCHLIST -> {
        val color = context.colorFromAttr(android.R.attr.textColorSecondary)
        val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)

        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        inMyShowsButton.run {
          setIconResource(R.drawable.ic_bookmark_full)
          setText(R.string.textInWatchlist)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
        archiveButton.run {
          fadeIn(duration, withHardware = true) { isEnabled = true }
          setColorFilter(color)
        }
      }
      State.IN_ARCHIVE -> {
        addToMyShowsButton.fadeOut(duration, withHardware = true)
        watchlistButton.fadeOut(duration, withHardware = true)
        inMyShowsButton.run {
          val color = context.colorFromAttr(android.R.attr.textColorSecondary)
          val colorState = context.colorStateListFromAttr(android.R.attr.textColorSecondary)
          setIconResource(R.drawable.ic_archive)
          setText(R.string.textInArchive)
          setTextColor(color)
          iconTint = colorState
          strokeColor = colorState
          rippleColor = colorState
          fadeIn(duration, withHardware = true) { isEnabled = true }
        }
        archiveButton.gone()
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    addToMyShowsButton.isEnabled = enabled
    watchlistButton.isEnabled = enabled
    inMyShowsButton.isEnabled = enabled
    archiveButton.isEnabled = enabled
  }

  enum class State {
    ADD,
    IN_MY_SHOWS,
    IN_WATCHLIST,
    IN_ARCHIVE
  }
}
