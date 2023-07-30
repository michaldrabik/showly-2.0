package com.michaldrabik.ui_discover_movies.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover_movies.databinding.ViewMoviePremiumBinding
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem

class MoviePremiumView : MovieView<DiscoverMovieListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMoviePremiumBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    binding.viewMoviePremiumRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = binding.viewMoviePremiumImageStub
  override val placeholderView: ImageView = binding.viewMoviePremiumImageStub

  private lateinit var item: DiscoverMovieListItem

  override fun bind(item: DiscoverMovieListItem) {
    super.bind(item)
    this.item = item
  }
}
