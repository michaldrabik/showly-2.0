package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.ADD
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.IN_MY_SHOWS
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.IN_WATCH_LATER
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_add_to_shows_button.view.*

class AddToShowsButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onAddMyShowsClickListener: () -> Unit = {}
  var onAddWatchLaterClickListener: () -> Unit = {}
  var onRemoveClickListener: () -> Unit = {}

  private var state: State = ADD

  init {
    inflate(context, R.layout.view_add_to_shows_button, this)

    addToMyShowsButton.onClick { onAddMyShowsClickListener() }
    seeLaterButton.onClick { onAddWatchLaterClickListener() }
    inMyShowsButton.onClick { onRemoveClickListener() }
  }

  fun setState(state: State, animate: Boolean = false) {
    this.state = state
    val duration = if (animate) 250L else 0
    if (animate) isEnabled = false
    when (state) {
      ADD -> {
        addToMyShowsButton.fadeIn(duration)
        seeLaterButton.fadeIn(duration)
        inMyShowsButton.fadeOut(duration) {
          isEnabled = true
        }
      }
      IN_MY_SHOWS -> {
        addToMyShowsButton.fadeOut(duration)
        seeLaterButton.fadeOut(duration)
        inMyShowsButton.fadeIn(duration) {
          isEnabled = true
        }
        inMyShowsButton.setText(R.string.textInMyShows)
      }
      IN_WATCH_LATER -> {
        addToMyShowsButton.fadeOut(duration)
        seeLaterButton.fadeOut(duration)
        inMyShowsButton.fadeIn(duration) {
          isEnabled = true
        }
        inMyShowsButton.setText(R.string.textInSeeLater)
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    addToMyShowsButton.isEnabled = enabled
    seeLaterButton.isEnabled = enabled
    inMyShowsButton.isEnabled = enabled
  }

  enum class State {
    ADD,
    IN_MY_SHOWS,
    IN_WATCH_LATER
  }
}