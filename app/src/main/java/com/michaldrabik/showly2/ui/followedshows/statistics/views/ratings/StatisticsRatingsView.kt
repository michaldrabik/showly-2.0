package com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings.recycler.StatisticsRatingItem
import com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings.recycler.StatisticsRatingsAdapter
import com.michaldrabik.showly2.utilities.extensions.addDivider
import com.michaldrabik.showly2.utilities.extensions.colorFromAttr
import kotlinx.android.synthetic.main.view_statistics_card_ratings.view.*

@SuppressLint("SetTextI18n")
class StatisticsRatingsView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val adapter by lazy { StatisticsRatingsAdapter() }
  private val layoutManager by lazy { LinearLayoutManager(context, HORIZONTAL, false) }

  var onShowClickListener: ((ListItem) -> Unit)? = null

  init {
    inflate(context, R.layout.view_statistics_card_ratings, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    clipToPadding = false
    clipChildren = false
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    setupRecycler()
  }

  private fun setupRecycler() {
    viewRatingsRecycler.apply {
      setHasFixedSize(true)
      adapter = this@StatisticsRatingsView.adapter
      layoutManager = this@StatisticsRatingsView.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      addDivider(R.drawable.divider_statistics_ratings, HORIZONTAL)
    }
    adapter.itemClickListener = {
      onShowClickListener?.invoke(it)
    }
  }

  fun bind(items: List<StatisticsRatingItem>) {
    adapter.setItems(items)
  }
}
