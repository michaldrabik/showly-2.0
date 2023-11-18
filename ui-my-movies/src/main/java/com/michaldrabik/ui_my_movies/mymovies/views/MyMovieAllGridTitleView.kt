package com.michaldrabik.ui_my_movies.mymovies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.databinding.ViewCollectionMovieGridTitleBinding
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import java.util.Locale

@SuppressLint("SetTextI18n")
class MyMovieAllGridTitleView : MovieView<MyMoviesItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionMovieGridTitleBinding.inflate(LayoutInflater.from(context), this)

  private val width by lazy {
    val span = if (context.isTablet()) Config.LISTS_GRID_SPAN_TABLET else Config.LISTS_GRID_SPAN
    val itemSpacing = context.dimenToPx(R.dimen.spaceSmall)
    val screenMargin = context.dimenToPx(R.dimen.screenMarginHorizontal)
    val screenWidth = screenWidth().toFloat()
    ((screenWidth - (screenMargin * 2.0)) - ((span - 1) * itemSpacing)) / span
  }
  private val height by lazy { width * 1.7305 }

  init {
    layoutParams = LayoutParams(width.toInt(), height.toInt())

    clipChildren = false
    clipToPadding = false

    with(binding) {
      collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
      collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionMovieImage
  override val placeholderView: ImageView = binding.collectionMoviePlaceholder

  private lateinit var item: MyMoviesItem

  override fun bind(item: MyMoviesItem) {
    clear()
    this.item = item

    with(binding) {
      collectionMovieProgress.visibleIf(item.isLoading)

      collectionMovieTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.movie.title
        else item.translation?.title

      if (item.sortOrder == SortOrder.RATING) {
        bindRating(item)
      } else if (item.sortOrder == SortOrder.USER_RATING && item.userRating != null) {
        collectionMovieRating.visible()
        collectionMovieRating.text = String.format(Locale.ENGLISH, "%d", item.userRating)
      } else {
        collectionMovieRating.gone()
      }
    }

    loadImage(item)
  }

  private fun bindRating(item: MyMoviesItem) {
    with(binding) {
      var rating = String.format(Locale.ENGLISH, "%.1f", item.movie.rating)

      if (item.spoilers.isSpoilerRatingsHidden) {
        collectionMovieRating.tag = rating
        rating = SPOILERS_RATINGS_HIDE_SYMBOL

        if (item.spoilers.isSpoilerTapToReveal) {
          with(collectionMovieRating) {
            onClick {
              tag?.let { text = it.toString() }
              isClickable = false
            }
          }
        }
      }

      collectionMovieRating.visible()
      collectionMovieRating.text = rating
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionMovieTitle.text = ""
      collectionMoviePlaceholder.gone()
      Glide.with(this@MyMovieAllGridTitleView).clear(collectionMovieImage)
    }
  }
}
