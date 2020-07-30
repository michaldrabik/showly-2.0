package com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_statistics_card_most_watched_shows.view.*

@SuppressLint("SetTextI18n")
class StatisticsMostWatchedShowsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onLoadMoreClickListener: ((Int) -> Unit)? = null

  init {
    inflate(context, R.layout.view_statistics_card_most_watched_shows, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  fun bind(
    items: List<StatisticsMostWatchedItem>,
    totalCount: Int
  ) {
    viewMostWatchedShowsItems.removeAllViews()
    items.forEach { item ->
      val view = StatisticsMostWatchedItemView(context).apply {
        bind(item)
      }
      viewMostWatchedShowsItems.addView(view)
    }

    viewMostWatchedShowsMoreButton.run {
      visibleIf(items.size < totalCount)
      onClick { onLoadMoreClickListener?.invoke(10) }
    }
  }
}
