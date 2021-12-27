package com.michaldrabik.ui_progress_movies.calendar.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import kotlinx.android.synthetic.main.view_progress_movies_calendar_item.view.*

@SuppressLint("SetTextI18n")
class CalendarMoviesItemView : MovieView<CalendarMovieListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_progress_movies_calendar_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: CalendarMovieListItem.MovieItem

  override val imageView: ImageView = progressMovieCalendarItemImage
  override val placeholderView: ImageView = progressMovieCalendarItemPlaceholder

  override fun bind(item: CalendarMovieListItem.MovieItem) {
    this.item = item
    clear()

    progressMovieCalendarItemTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    progressMovieCalendarItemSubtitle.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          if (item.movie.overview.isBlank()) context.getString(R.string.textNoDescription)
          else item.movie.overview
        }
        else -> item.translation?.overview
      }

    if (item.movie.released != null) {
      progressMovieCalendarItemDate.text = item.dateFormat?.format(item.movie.released)?.capitalizeWords()
    } else {
      progressMovieCalendarItemDate.text = context.getString(R.string.textTba)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    progressMovieCalendarItemTitle.text = ""
    progressMovieCalendarItemSubtitle.text = ""
    progressMovieCalendarItemPlaceholder.gone()
    Glide.with(this).clear(progressMovieCalendarItemImage)
  }
}
