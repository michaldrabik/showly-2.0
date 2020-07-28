package com.michaldrabik.showly2.ui.watchlist.pages.upcoming.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.michaldrabik.showly2.R
import kotlinx.android.synthetic.main.view_watchlist_upcoming_header.view.*

@SuppressLint("SetTextI18n")
class WatchlistUpcomingHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_watchlist_upcoming_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(@StringRes textResId: Int) {
    watchlistUpcomingHeaderText.setText(textResId)
  }
}
