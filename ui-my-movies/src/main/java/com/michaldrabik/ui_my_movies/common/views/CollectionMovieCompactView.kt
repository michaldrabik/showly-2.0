package com.michaldrabik.ui_my_movies.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_movies.databinding.ViewCollectionMovieCompactBinding
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionMovieCompactView : MovieView<CollectionListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionMovieCompactBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

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

  private val nowUtc = nowUtcDay()
  private lateinit var item: CollectionListItem.MovieItem

  override fun bind(item: CollectionListItem.MovieItem) {
    clear()
    this.item = item

    with(binding) {
      collectionMovieProgress.visibleIf(item.isLoading)
      collectionMovieTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.movie.title
        else item.translation?.title

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

      bindRating(item)
      item.userRating?.let {
        collectionMovieUserStarIcon.visible()
        collectionMovieUserRating.visible()
        collectionMovieUserRating.text = String.format(ENGLISH, "%d", it)
      }
    }

    loadImage(item)
  }

  private fun bindRating(item: CollectionListItem.MovieItem) {
    with(binding) {
      var rating = String.format(ENGLISH, "%.1f", item.movie.rating)

      if (item.spoilers.isSpoilerRatingsHidden) {
        collectionMovieRating.tag = rating
        rating = Config.SPOILERS_RATINGS_HIDE_SYMBOL

        if (item.spoilers.isSpoilerTapToReveal) {
          with(collectionMovieRating) {
            onClick {
              tag?.let { text = it.toString() }
              isClickable = false
            }
          }
        }
      }

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
      collectionMovieYear.text = ""
      collectionMovieRating.text = ""
      collectionMoviePlaceholder.gone()
      collectionMovieUserRating.gone()
      collectionMovieUserStarIcon.gone()
      Glide.with(this@CollectionMovieCompactView).clear(collectionMovieImage)
    }
  }
}
