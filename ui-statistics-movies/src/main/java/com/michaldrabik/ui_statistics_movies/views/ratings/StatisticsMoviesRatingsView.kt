package com.michaldrabik.ui_statistics_movies.views.ratings

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_statistics_movies.R
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingsAdapter
import kotlinx.android.synthetic.main.view_statistics_movies_card_ratings.view.*

@SuppressLint("SetTextI18n")
class StatisticsMoviesRatingsView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val adapter by lazy {
    StatisticsMoviesRatingsAdapter(
      itemClickListener = { onMovieClickListener?.invoke(it) }
    )
  }
  private val layoutManager by lazy { LinearLayoutManager(context, HORIZONTAL, false) }

  var onMovieClickListener: ((MovieListItem) -> Unit)? = null

  init {
    inflate(context, R.layout.view_statistics_movies_card_ratings, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    clipToPadding = false
    clipChildren = false
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    setupRecycler()
  }

  private fun setupRecycler() {
    viewMoviesRatingsRecycler.apply {
      setHasFixedSize(true)
      adapter = this@StatisticsMoviesRatingsView.adapter
      layoutManager = this@StatisticsMoviesRatingsView.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      addDivider(R.drawable.divider_statistics_ratings, HORIZONTAL)
    }
  }

  fun bind(items: List<StatisticsMoviesRatingItem>) {
    adapter.setItems(items)
  }
}
