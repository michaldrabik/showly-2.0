package com.michaldrabik.ui_progress_movies.calendar.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.databinding.ViewProgressMoviesCalendarItemBinding

@SuppressLint("SetTextI18n")
class CalendarMoviesItemView : MovieView<CalendarMovieListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressMoviesCalendarItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: CalendarMovieListItem.MovieItem

  override val imageView: ImageView = binding.progressMovieCalendarItemImage
  override val placeholderView: ImageView = binding.progressMovieCalendarItemPlaceholder

  override fun bind(item: CalendarMovieListItem.MovieItem) {
    this.item = item
    clear()

    with(binding) {
      progressMovieCalendarItemTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.movie.title
        else item.translation?.title

      bindDescription(item)
      bindBadge(item)

      if (item.movie.released != null) {
        progressMovieCalendarItemDate.text = item.dateFormat?.format(item.movie.released)?.capitalizeWords()
      } else {
        progressMovieCalendarItemDate.text = context.getString(R.string.textTba)
      }

      loadImage(item)
    }
  }

  private fun bindBadge(item: CalendarMovieListItem.MovieItem) {
    with(binding.progressMovieCalendarItemBadge) {
      val inCollection = item.isWatched || item.isWatchlist
      visibleIf(inCollection)
      if (inCollection) {
        val color = if (item.isWatched) R.color.colorAccent else R.color.colorGrayLight
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
      }
    }
  }

  private fun bindDescription(item: CalendarMovieListItem.MovieItem) {
    var description = if (item.translation?.overview.isNullOrBlank()) {
      item.movie.overview.ifBlank { context.getString(R.string.textNoDescription) }
    } else {
      item.translation?.overview
    }

    with(binding) {
      val isMyHidden = item.spoilers.isMyMoviesHidden && item.isWatched
      val isWatchlistHidden = item.spoilers.isWatchlistMoviesHidden && item.isWatchlist
      val isNotCollectedHidden = item.spoilers.isNotCollectedMoviesHidden && (!item.isWatched && !item.isWatchlist)
      if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
        progressMovieCalendarItemSubtitle.tag = description
        description = SPOILERS_REGEX.replace(description.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isTapToReveal) {
          progressMovieCalendarItemSubtitle.onClick { view ->
            view.tag?.let { progressMovieCalendarItemSubtitle.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      progressMovieCalendarItemSubtitle.text = description
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      progressMovieCalendarItemTitle.text = ""
      progressMovieCalendarItemSubtitle.text = ""
      progressMovieCalendarItemPlaceholder.gone()
      Glide.with(this@CalendarMoviesItemView).clear(progressMovieCalendarItemImage)
    }
  }
}
