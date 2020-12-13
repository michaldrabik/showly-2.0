package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_shows_movies_tabs.view.*

class ShowsMoviesTabsView : LinearLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_shows_movies_tabs, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    orientation = HORIZONTAL

    viewMovies.onClick { onModeSelected?.invoke(MOVIES) }
    viewShows.onClick { onModeSelected?.invoke(SHOWS) }
  }

  var onModeSelected: ((Mode) -> Unit)? = null

  fun animateMovies() {
    viewShows.alpha = 0.5F
    viewMovies.alpha = 1F
  }

  fun animateShows() {
    viewShows.alpha = 1F
    viewMovies.alpha = 0.5F
  }

  override fun setEnabled(enabled: Boolean) {
    viewShows.isEnabled = enabled
    viewMovies.isEnabled = enabled
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
