package com.michaldrabik.ui_lists.details.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.Movie
import kotlinx.android.synthetic.main.view_list_details_movie_item.view.*
import kotlinx.android.synthetic.main.view_list_details_show_item.view.*
import java.util.Locale.ENGLISH
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class ListDetailsMovieItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_movie_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setBackgroundColor(context.colorFromAttr(android.R.attr.windowBackground))

    imageLoadCompleteListener = {
      if (item.translation == null) {
        missingTranslationListener?.invoke(item)
      }
    }

    listDetailsMovieHandle.expandTouch(100)
    listDetailsMovieHandle.setOnTouchListener { _, event ->
      if (item.isManageMode && event.action == ACTION_DOWN) {
        itemDragStartListener?.invoke()
      }
      false
    }

    var x = 0F
    listDetailsMovieRoot.setOnTouchListener { _, event ->
      if (item.isManageMode) {
        return@setOnTouchListener false
      }
      if (event.action == ACTION_DOWN) x = event.x
      if (event.action == ACTION_UP) x = 0F
      if (event.action == ACTION_MOVE && abs(x - event.x) > 50F) {
        itemSwipeStartListener?.invoke()
        return@setOnTouchListener true
      }
      false
    }

    listDetailsMovieRoot.onClick {
      if (item.isEnabled && !item.isManageMode) itemClickListener?.invoke(item)
    }
  }

  override val imageView: ImageView = listDetailsMovieImage
  override val placeholderView: ImageView = listDetailsMoviePlaceholder

  override fun bind(item: ListDetailsItem) {
    super.bind(item)
    Glide.with(this).clear(listDetailsMovieImage)
    val movie = item.requireMovie()

    listDetailsMovieProgress.visibleIf(item.isLoading)

    listDetailsMovieTitle.text =
      if (item.translation?.title.isNullOrBlank()) movie.title
      else item.translation?.title

    bindDescription(item, movie)
    bindRating(item, movie)

    listDetailsMovieHeader.text = String.format(ENGLISH, "%d", movie.year)
    listDetailsMovieUserRating.text = String.format(ENGLISH, "%d", item.userRating)

    listDetailsMovieRank.visibleIf(item.isRankDisplayed)
    listDetailsMovieRank.text = String.format(ENGLISH, "%d", item.rankDisplay)

    listDetailsMovieHandle.visibleIf(item.isManageMode)
    listDetailsMovieStarIcon.visibleIf(!item.isManageMode)
    listDetailsMovieUserStarIcon.visibleIf(!item.isManageMode && item.userRating != null)
    listDetailsMovieUserRating.visibleIf(!item.isManageMode && item.userRating != null)

    with(listDetailsMovieHeaderBadge) {
      val inCollection = item.isWatched || item.isWatchlist
      visibleIf(inCollection)
      if (inCollection) {
        val color = if (item.isWatched) R.color.colorAccent else R.color.colorGrayLight
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
      }
    }

    listDetailsMovieRoot.alpha = if (item.isEnabled) 1F else 0.45F
    loadImage(item)
  }

  private fun bindDescription(
    item: ListDetailsItem,
    movie: Movie,
  ) {
    var description =
      when {
        item.translation?.overview.isNullOrBlank() -> movie.overview.ifBlank {
          context.getString(R.string.textNoDescription)
        }
        else -> item.translation?.overview
      }

    val isMyHidden = item.spoilers.isMyMoviesHidden && item.isWatched
    val isWatchlistHidden = item.spoilers.isWatchlistMoviesHidden && item.isWatchlist
    val isNotCollectedHidden = item.spoilers.isNotCollectedMoviesHidden && (!item.isWatched && !item.isWatchlist)
    if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
      listDetailsMovieDescription.tag = description
      description = SPOILERS_REGEX.replace(description.toString(), Config.SPOILERS_HIDE_SYMBOL)
    }

    listDetailsMovieDescription.text = description
    if (item.spoilers.isTapToReveal) {
      with(listDetailsMovieDescription) {
        onClick {
          tag?.let { text = it.toString() }
          isClickable = false
        }
      }
    }
  }

  private fun bindRating(
    item: ListDetailsItem,
    movie: Movie,
  ) {
    var rating = String.format(ENGLISH, "%.1f", movie.rating)

    val isMyHidden = item.spoilers.isMyMoviesRatingsHidden && item.isWatched
    val isWatchlistHidden = item.spoilers.isWatchlistMoviesRatingsHidden && item.isWatchlist
    val isNotCollectedHidden = item.spoilers.isNotCollectedMoviesRatingsHidden && (!item.isWatched && !item.isWatchlist)
    if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
      listDetailsMovieRating.tag = rating
      rating = SPOILERS_RATINGS_HIDE_SYMBOL
    }

    listDetailsMovieRating.visibleIf(!item.isManageMode)
    listDetailsMovieRating.text = rating

    if (item.spoilers.isTapToReveal) {
      with(listDetailsMovieRating) {
        onClick {
          tag?.let { text = it.toString() }
          isClickable = false
        }
      }
    }
  }
}
