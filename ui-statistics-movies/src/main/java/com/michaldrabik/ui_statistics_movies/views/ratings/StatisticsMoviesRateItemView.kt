package com.michaldrabik.ui_statistics_movies.views.ratings

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_statistics_movies.R
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem
import kotlinx.android.synthetic.main.view_statistics_movies_rate_item.view.*

class StatisticsMoviesRateItemView : MovieView<StatisticsMoviesRatingItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_movies_rate_item, this)
    val width = context.dimenToPx(R.dimen.statisticsMoviesRatingItemWidth)
    layoutParams = LayoutParams(width, WRAP_CONTENT)
    clipChildren = false
    viewMovieRateItemImageLayout.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewMovieRateItemImage
  override val placeholderView: ImageView = viewMovieRateItemPlaceholder

  private lateinit var item: StatisticsMoviesRatingItem

  override fun bind(item: StatisticsMoviesRatingItem) {
    this.item = item
    clear()
    viewMovieRateItemTitle.text = item.movie.title
    viewMovieRateItemRating.text = "${item.rating.rating}"
    loadImage(item)
  }

  private fun clear() {
    viewMovieRateItemTitle.gone()
    viewMovieRateItemPlaceholder.gone()
    Glide.with(this).clear(viewMovieRateItemImage)
  }
}
