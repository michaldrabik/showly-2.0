package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover_movies.R
import com.michaldrabik.ui_discover_movies.databinding.ViewMovieFanartBinding
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE

class MovieFanartView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMovieFanartBinding.inflate(LayoutInflater.from(context), this)

  init {
    with(binding.movieFanartRoot) {
      onClick { itemClickListener?.invoke(item) }
      onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = binding.movieFanartImage
  override val placeholderView: ImageView = binding.movieFanartPlaceholder

  private lateinit var item: DiscoverMovieListItem

  override fun bind(item: DiscoverMovieListItem) {
    super.bind(item)
    clear()
    this.item = item
    with(binding) {
      movieFanartTitle.text =
        if (item.translation?.title.isNullOrBlank()) {
          item.movie.title
        } else {
          item.translation?.title
        }
      movieFanartProgress.visibleIf(item.isLoading)
      movieFanartBadge.visibleIf(item.isCollected)
      movieFanartBadgeLater.visibleIf(item.isWatchlist)
    }
    loadImage(item)
  }

  override fun loadImage(item: DiscoverMovieListItem) {
    super.loadImage(item)
    if (item.image.status == UNAVAILABLE) {
      binding.movieFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  override fun onImageLoadFail(item: DiscoverMovieListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      binding.movieFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    with(binding) {
      movieFanartTitle.text = ""
      movieFanartProgress.gone()
      movieFanartPlaceholder.gone()
      movieFanartRoot.setBackgroundResource(R.drawable.bg_media_view_elevation)
      movieFanartBadge.gone()
      Glide.with(this@MovieFanartView).clear(movieFanartImage)
    }
  }
}
