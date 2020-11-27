package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.MovieImage.Status
import kotlinx.android.synthetic.main.view_movie_fanart.view.*

class MovieFanartView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_movie_fanart, this)
    movieFanartRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = movieFanartImage
  override val placeholderView: ImageView = movieFanartPlaceholder

  private lateinit var item: DiscoverMovieListItem

  override fun bind(
    item: DiscoverMovieListItem,
    missingImageListener: ((DiscoverMovieListItem, Boolean) -> Unit)?
  ) {
    super.bind(item, missingImageListener)
    clear()
    this.item = item
    movieFanartTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title?.capitalizeWords()
    movieFanartProgress.visibleIf(item.isLoading)
    movieFanartBadge.visibleIf(item.isCollected)
    movieFanartBadgeLater.visibleIf(item.isWatchlist)
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: DiscoverMovieListItem, missingImageListener: ((DiscoverMovieListItem, Boolean) -> Unit)?) {
    super.loadImage(item, missingImageListener)
    if (item.image.status == Status.UNAVAILABLE) {
      movieFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    movieFanartTitle.text = ""
    movieFanartProgress.gone()
    movieFanartPlaceholder.gone()
    movieFanartRoot.setBackgroundResource(0)
    movieFanartBadge.gone()
    Glide.with(this).clear(movieFanartImage)
  }
}
