package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.Image.Status.AVAILABLE
import com.michaldrabik.ui_model.Image.Status.UNAVAILABLE
import kotlinx.android.synthetic.main.view_movie_poster.view.*

class MoviePosterView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_movie_poster, this)
    moviePosterRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = moviePosterImage
  override val placeholderView: ImageView = moviePosterPlaceholder

  private lateinit var item: DiscoverMovieListItem

  override fun bind(
    item: DiscoverMovieListItem,
    missingImageListener: ((DiscoverMovieListItem, Boolean) -> Unit)?
  ) {
    super.bind(item, missingImageListener)
    clear()
    this.item = item
    moviePosterTitle.text = item.movie.title
    moviePosterProgress.visibleIf(item.isLoading)
    moviePosterBadge.visibleIf(item.isCollected)
    moviePosterLaterBadge.visibleIf(item.isWatchlist)
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: DiscoverMovieListItem, missingImageListener: ((DiscoverMovieListItem, Boolean) -> Unit)?) {
    if (item.image.status == UNAVAILABLE) {
      moviePosterTitle.visible()
      moviePosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
    super.loadImage(item, missingImageListener)
  }

  override fun onImageLoadSuccess() {
    moviePosterTitle.gone()
    moviePosterRoot.setBackgroundResource(0)
  }

  override fun onImageLoadFail(item: DiscoverMovieListItem, missingImageListener: ((DiscoverMovieListItem, Boolean) -> Unit)?) {
    super.onImageLoadFail(item, missingImageListener)
    if (item.image.status == AVAILABLE) {
      moviePosterTitle.visible()
      moviePosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    moviePosterTitle.text = ""
    moviePosterTitle.gone()
    moviePosterRoot.setBackgroundResource(0)
    moviePosterPlaceholder.gone()
    moviePosterProgress.gone()
    moviePosterBadge.gone()
    Glide.with(this).clear(moviePosterImage)
  }
}
