package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover_movies.R
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import kotlinx.android.synthetic.main.view_movie_poster.view.*

class MoviePosterView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_movie_poster, this)
    with(moviePosterRoot) {
      onClick { itemClickListener?.invoke(item) }
      onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = moviePosterImage
  override val placeholderView: ImageView = moviePosterPlaceholder

  private lateinit var item: DiscoverMovieListItem

  override fun bind(item: DiscoverMovieListItem) {
    super.bind(item)
    clear()
    this.item = item
    moviePosterTitle.text = item.movie.title
    moviePosterProgress.visibleIf(item.isLoading)
    moviePosterBadge.visibleIf(item.isCollected)
    moviePosterLaterBadge.visibleIf(item.isWatchlist)
    loadImage(item)
  }

  override fun loadImage(item: DiscoverMovieListItem) {
    if (item.image.status == UNAVAILABLE) {
      moviePosterTitle.visible()
      moviePosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: DiscoverMovieListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      moviePosterTitle.visible()
      moviePosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    moviePosterTitle.text = ""
    moviePosterTitle.gone()
    moviePosterRoot.setBackgroundResource(R.drawable.bg_media_view_elevation)
    moviePosterPlaceholder.gone()
    moviePosterProgress.gone()
    moviePosterBadge.gone()
    Glide.with(this).clear(moviePosterImage)
  }
}
