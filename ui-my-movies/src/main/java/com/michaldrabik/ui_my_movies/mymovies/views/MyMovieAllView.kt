package com.michaldrabik.ui_my_movies.mymovies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import kotlinx.android.synthetic.main.view_collection_movie.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyMovieAllView : MovieView<MyMoviesItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_collection_movie, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    imageLoadCompleteListener = { loadTranslation() }
    collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
    collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
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

    bindDescription(item)
    bindRating(item)

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

  private fun bindDescription(item: MyMoviesItem) {
    var description =
      if (item.translation?.overview.isNullOrBlank()) item.movie.overview
      else item.translation?.overview

    if (item.spoilers.isSpoilerHidden) {
      collectionMovieDescription.tag = description.toString()
      description = SPOILERS_REGEX.replace(description.toString(), SPOILERS_HIDE_SYMBOL)

      if (item.spoilers.isSpoilerTapToReveal) {
        collectionMovieDescription.onClick { view ->
          view.tag?.let {
            collectionMovieDescription.text = it.toString()
          }
          view.isClickable = false
        }
      }
    }

    collectionMovieDescription.text = description
    collectionMovieDescription.visibleIf(item.movie.overview.isNotBlank())
  }

  private fun bindRating(item: MyMoviesItem) {
    var rating = String.format(ENGLISH, "%.1f", item.movie.rating)

    if (item.spoilers.isSpoilerRatingsHidden) {
      collectionMovieRating.tag = rating
      rating = SPOILERS_RATINGS_HIDE_SYMBOL

      if (item.spoilers.isSpoilerTapToReveal) {
        collectionMovieRating.onClick { view ->
          view.tag?.let {
            collectionMovieRating.text = it.toString()
          }
          view.isClickable = false
        }
      }
    }

    collectionMovieRating.text = rating
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    collectionMovieTitle.text = ""
    collectionMovieDescription.text = ""
    collectionMovieYear.text = ""
    collectionMovieRating.text = ""
    collectionMovieUserRating.gone()
    collectionMovieUserStarIcon.gone()
    collectionMoviePlaceholder.gone()
    Glide.with(this).clear(collectionMovieImage)
  }
}
