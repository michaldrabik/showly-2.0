package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
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

    viewMovies.onClick { setMovies() }
    viewShows.onClick { setShows() }
  }

  var onMoviesSet: (() -> Unit)? = null
  var onShowsSet: (() -> Unit)? = null

  private fun setMovies() {
    viewShows.animate().alpha(0.5F).setDuration(100).start()
    viewMovies.animate().alpha(1F).setDuration(100).start()
    onMoviesSet?.invoke()
  }

  private fun setShows() {
    viewShows.animate().alpha(1F).setDuration(100).start()
    viewMovies.animate().alpha(0.5F).setDuration(100).start()
    onShowsSet?.invoke()
  }

  override fun setEnabled(enabled: Boolean) {
    viewShows.isEnabled = enabled
    viewMovies.isEnabled = enabled
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
