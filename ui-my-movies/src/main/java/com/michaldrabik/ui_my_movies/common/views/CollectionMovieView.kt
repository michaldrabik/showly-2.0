package com.michaldrabik.ui_my_movies.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import kotlinx.android.synthetic.main.view_collection_movie.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionMovieView : MovieView<CollectionListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_collection_movie, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
    collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = collectionMovieImage
  override val placeholderView: ImageView = collectionMoviePlaceholder

  private var nowUtc = nowUtcDay()
  private lateinit var item: CollectionListItem.MovieItem

  override fun bind(item: CollectionListItem.MovieItem) {
    clear()
    this.item = item
    collectionMovieProgress.visibleIf(item.isLoading)
    collectionMovieTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    collectionMovieDescription.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          item.movie.overview.ifBlank {
            context.getString(R.string.textNoDescription)
          }
        }
        else -> item.translation?.overview
      }

    val releaseDate = item.movie.released
    val isUpcoming = releaseDate?.let { it.toEpochDay() > nowUtc.toEpochDay() } ?: false

    with(collectionMovieYear) {
      when {
        isUpcoming -> gone()
        releaseDate != null -> {
          visible()
          text = item.dateFormat.format(releaseDate)?.capitalizeWords()
        }
        item.movie.year > 0 -> {
          visible()
          text = String.format(ENGLISH, "%d", item.movie.year)
        }
      }
    }

    with(collectionMovieReleaseDate) {
      visibleIf(isUpcoming)
      releaseDate?.let {
        text = item.fullDateFormat.format(it)?.capitalizeWords()
      }
    }

    collectionMovieRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
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
    collectionMovieDescription.text = ""
    collectionMovieYear.text = ""
    collectionMovieRating.text = ""
    collectionMovieYear.gone()
    collectionMoviePlaceholder.gone()
    collectionMovieUserStarIcon.gone()
    collectionMovieUserRating.gone()
    collectionMovieReleaseDate.gone()
    Glide.with(this).clear(collectionMovieImage)
  }
}
