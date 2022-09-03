package com.michaldrabik.ui_my_movies.watchlist.views

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
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
import kotlinx.android.synthetic.main.view_watchlist_movie.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class WatchlistMovieView : MovieView<WatchlistListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_watchlist_movie, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    imageLoadCompleteListener = { loadTranslation() }
    with(watchlistMoviesRoot) {
      onClick { itemClickListener?.invoke(item) }
      onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = watchlistMoviesImage
  override val placeholderView: ImageView = watchlistMoviesPlaceholder

  private var nowUtc = nowUtcDay()
  private lateinit var item: WatchlistListItem.MovieItem

  override fun bind(item: WatchlistListItem.MovieItem) {
    clear()
    this.item = item
    watchlistMoviesProgress.visibleIf(item.isLoading)
    watchlistMoviesTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    watchlistMoviesDescription.text =
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

    with(watchlistMoviesYear) {
      when {
        isUpcoming -> gone()
        releaseDate != null -> {
          visible()
          text = item.dateFormat?.format(releaseDate)?.capitalizeWords()
        }
        item.movie.year > 0 -> {
          visible()
          text = String.format(ENGLISH, "%d", item.movie.year)
        }
      }
    }

    with(watchlistMovieReleaseDate) {
      visibleIf(isUpcoming)
      releaseDate?.let {
        text = item.fullDateFormat?.format(it)?.capitalizeWords()
      }
    }

    watchlistMoviesRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
    item.userRating?.let {
      watchlistMovieUserStarIcon.visible()
      watchlistMovieUserRating.visible()
      watchlistMovieUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    watchlistMoviesTitle.text = ""
    watchlistMoviesDescription.text = ""
    watchlistMoviesYear.text = ""
    watchlistMoviesRating.text = ""
    watchlistMoviesYear.gone()
    watchlistMoviesPlaceholder.gone()
    watchlistMovieUserStarIcon.gone()
    watchlistMovieUserRating.gone()
    watchlistMovieReleaseDate.gone()
    Glide.with(this).clear(watchlistMoviesImage)
  }
}
