package com.michaldrabik.ui_my_movies.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.setOutboundRipple
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_movies.databinding.ViewCollectionMovieBinding
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionMovieView : MovieView<CollectionListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionMovieBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    clipChildren = false
    clipToPadding = false

    with(binding) {
      collectionMovieRoot.onClick { itemClickListener?.invoke(item) }
      collectionMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
      collectionMovieRoot.setOutboundRipple(
        size = (context.dimenToPx(R.dimen.collectionItemRippleSpace)).toFloat(),
        corner = context.dimenToPx(R.dimen.mediaTileCorner).toFloat()
      )
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionMovieImage
  override val placeholderView: ImageView = binding.collectionMoviePlaceholder

  private var nowUtc = nowUtcDay()
  private lateinit var item: CollectionListItem.MovieItem

  override fun bind(item: CollectionListItem.MovieItem) {
    clear()
    this.item = item

    with(binding) {
      collectionMovieProgress.visibleIf(item.isLoading)
      collectionMovieTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.movie.title
        else item.translation?.title

      bindDescription(item)
      bindRating(item)

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

      item.userRating?.let {
        collectionMovieUserStarIcon.visible()
        collectionMovieUserRating.visible()
        collectionMovieUserRating.text = String.format(ENGLISH, "%d", it)
      }
    }

    loadImage(item)
  }

  private fun bindDescription(item: CollectionListItem.MovieItem) {
    with(binding) {
      collectionMovieDescription.text =
        if (item.translation?.overview.isNullOrBlank()) item.movie.overview
        else item.translation?.overview

      if (item.spoilers.isSpoilerHidden) {
        collectionMovieDescription.tag = collectionMovieDescription.text
        collectionMovieDescription.text =
          SPOILERS_REGEX.replace(collectionMovieDescription.text, SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isSpoilerTapToReveal) {
          collectionMovieDescription.onClick { view ->
            view.tag?.let {
              collectionMovieDescription.text = it.toString()
            }
            view.isClickable = false
          }
        }
      }

      collectionMovieDescription.visibleIf(item.movie.overview.isNotBlank())
    }
  }

  private fun bindRating(item: CollectionListItem.MovieItem) {
    with(binding) {
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
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionMovieTitle.text = ""
      collectionMovieDescription.text = ""
      collectionMovieYear.text = ""
      collectionMovieRating.text = ""
      collectionMovieYear.gone()
      collectionMoviePlaceholder.gone()
      collectionMovieUserStarIcon.gone()
      collectionMovieUserRating.gone()
      collectionMovieReleaseDate.gone()
      Glide.with(this@CollectionMovieView).clear(collectionMovieImage)
    }
  }
}
