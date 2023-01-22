package com.michaldrabik.ui_my_movies.mymovies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_my_movies.databinding.ViewCollectionMovieGridBinding
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyMovieAllGridView : MovieView<MyMoviesItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionMovieGridBinding.inflate(LayoutInflater.from(context), this)

  private val gridPadding by lazy { context.dimenToPx(R.dimen.gridListsPadding) }
  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / Config.LISTS_GRID_SPAN }
  private val height by lazy { width * ASPECT_RATIO }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    binding.collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
    binding.collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionMovieImage
  override val placeholderView: ImageView = binding.collectionMoviePlaceholder

  private lateinit var item: MyMoviesItem

  override fun bind(item: MyMoviesItem) {
    layoutParams = LayoutParams(
      (width * item.image.type.spanSize.toFloat()).toInt(),
      height.toInt()
    )

    clear()
    this.item = item

    with(binding) {
      collectionMovieProgress.visibleIf(item.isLoading)

      if (item.sortOrder == RATING) {
        collectionMovieRating.visible()
        collectionMovieRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
      } else if (item.sortOrder == USER_RATING && item.userRating != null) {
        collectionMovieRating.visible()
        collectionMovieRating.text = String.format(ENGLISH, "%d", item.userRating)
      } else {
        collectionMovieRating.gone()
      }
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionMoviePlaceholder.gone()
      Glide.with(this@MyMovieAllGridView).clear(collectionMovieImage)
    }
  }
}
