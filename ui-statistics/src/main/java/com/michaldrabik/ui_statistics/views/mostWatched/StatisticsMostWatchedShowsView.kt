package com.michaldrabik.ui_statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_statistics.R
import com.michaldrabik.ui_statistics.views.mostWatched.recycler.MostWatchedAdapter
import kotlinx.android.synthetic.main.view_statistics_card_most_watched_shows.view.*

@SuppressLint("SetTextI18n")
class StatisticsMostWatchedShowsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var adapter: MostWatchedAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  var onLoadMoreClickListener: ((Int) -> Unit)? = null
  var onShowClickListener: ((Show) -> Unit)? = null

  init {
    inflate(context, R.layout.view_statistics_card_most_watched_shows, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = MostWatchedAdapter(
      itemClickListener = { onShowClickListener?.invoke(it.show) }
    )
    viewMostWatchedShowsRecycler.apply {
      adapter = this@StatisticsMostWatchedShowsView.adapter
      layoutManager = this@StatisticsMostWatchedShowsView.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      addDivider(R.drawable.divider_statistics_most_watched)
    }
  }

  fun bind(
    items: List<StatisticsMostWatchedItem>,
    totalCount: Int
  ) {
    adapter?.setItems(items)
    viewMostWatchedShowsMoreButton.run {
      visibleIf(items.size < totalCount)
      onClick { onLoadMoreClickListener?.invoke(10) }
    }
  }
}
