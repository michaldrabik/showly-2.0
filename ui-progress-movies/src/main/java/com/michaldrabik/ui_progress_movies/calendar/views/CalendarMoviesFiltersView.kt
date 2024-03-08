package com.michaldrabik.ui_progress_movies.calendar.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.databinding.ViewCalendarMoviesFiltersBinding

internal class CalendarMoviesFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCalendarMoviesFiltersBinding.inflate(LayoutInflater.from(context), this)

  private lateinit var filters: CalendarMovieListItem.Filters

  var onModeChipClick: ((CalendarMode) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      modeChip.onClick {
        onModeChipClick?.invoke(filters.mode)
      }
    }
  }

  fun bind(filters: CalendarMovieListItem.Filters) {
    this.filters = filters
    with(binding) {
      when (filters.mode) {
        CalendarMode.PRESENT_FUTURE -> {
          modeChip.text = context.getText(R.string.textWatchlistIncoming)
          modeChip.setChipIconResource(R.drawable.ic_calendar)
        }
        CalendarMode.RECENTS -> {
          modeChip.text = context.getText(R.string.textMovieStatusReleased)
          modeChip.setChipIconResource(R.drawable.ic_history)
        }
      }
    }
  }
}
