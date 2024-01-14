package com.michaldrabik.ui_statistics_movies.views.ratings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_statistics_movies.R
import com.michaldrabik.ui_statistics_movies.databinding.ViewStatisticsMoviesRateItemBinding
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem

class StatisticsMoviesRateItemView : MovieView<StatisticsMoviesRatingItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewStatisticsMoviesRateItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    val width = context.dimenToPx(R.dimen.statisticsMoviesRatingItemWidth)
    layoutParams = LayoutParams(width, WRAP_CONTENT)
    clipChildren = false
    binding.viewMovieRateItemImageLayout.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = binding.viewMovieRateItemImage
  override val placeholderView: ImageView = binding.viewMovieRateItemPlaceholder

  private lateinit var item: StatisticsMoviesRatingItem

  override fun bind(item: StatisticsMoviesRatingItem) {
    this.item = item
    clear()
    with(binding) {
      viewMovieRateItemTitle.text = item.movie.title
      viewMovieRateItemRating.text = "${item.rating.rating}"
    }
    loadImage(item)
  }

  private fun clear() {
    with(binding) {
      viewMovieRateItemTitle.gone()
      viewMovieRateItemPlaceholder.gone()
      Glide.with(this@StatisticsMoviesRateItemView).clear(viewMovieRateItemImage)
    }
  }
}
