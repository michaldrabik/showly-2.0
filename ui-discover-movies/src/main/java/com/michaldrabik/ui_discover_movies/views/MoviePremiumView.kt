package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover_movies.R
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import kotlinx.android.synthetic.main.view_movie_premium.view.*

class MoviePremiumView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_movie_premium, this)
    viewMoviePremiumRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewMoviePremiumImageStub
  override val placeholderView: ImageView = viewMoviePremiumImageStub

  private lateinit var item: DiscoverMovieListItem

  override fun bind(item: DiscoverMovieListItem) {
    super.bind(item)
    this.item = item
  }
}
