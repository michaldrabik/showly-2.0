package com.michaldrabik.ui_my_movies.mymovies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import kotlinx.android.synthetic.main.view_collection_movie_compact.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyMovieAllCompactView : MovieView<MyMoviesItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_collection_movie_compact, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
    collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = collectionMovieImage
  override val placeholderView: ImageView = collectionMoviePlaceholder

  private lateinit var item: MyMoviesItem

  override fun bind(item: MyMoviesItem) {
    clear()
    this.item = item
    collectionMovieProgress.visibleIf(item.isLoading)
    collectionMovieTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    collectionMovieRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
    collectionMovieYear.visibleIf(item.movie.released != null || item.movie.year > 0)
    collectionMovieYear.text = when {
      item.movie.released != null -> item.dateFormat?.format(item.movie.released)?.capitalizeWords()
      else -> String.format(ENGLISH, "%d", item.movie.year)
    }

    item.userRating?.let {
      collectionMovieUserStarIcon.visible()
      collectionMovieUserRating.visible()
      collectionMovieUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    collectionMovieTitle.text = ""
    collectionMovieYear.text = ""
    collectionMovieRating.text = ""
    collectionMovieUserRating.gone()
    collectionMovieUserStarIcon.gone()
    collectionMoviePlaceholder.gone()
    Glide.with(this).clear(collectionMovieImage)
  }
}
