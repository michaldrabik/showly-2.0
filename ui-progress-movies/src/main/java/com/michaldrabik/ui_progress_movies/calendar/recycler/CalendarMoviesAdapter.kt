package com.michaldrabik.ui_progress_movies.calendar.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.calendar.views.CalendarMoviesFiltersView
import com.michaldrabik.ui_progress_movies.calendar.views.CalendarMoviesHeaderView
import com.michaldrabik.ui_progress_movies.calendar.views.CalendarMoviesItemView

class CalendarMoviesAdapter(
  private val itemClickListener: (CalendarMovieListItem) -> Unit,
  private val itemLongClickListener: (CalendarMovieListItem) -> Unit,
  private val missingImageListener: (CalendarMovieListItem, Boolean) -> Unit,
  private val missingTranslationListener: (CalendarMovieListItem) -> Unit,
  var modeClickListener: ((CalendarMode) -> Unit),
) : BaseMovieAdapter<CalendarMovieListItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_FILTERS = 3
  }

  override val asyncDiffer = AsyncListDiffer(this, CalendarMovieItemDiffCallback())

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ) = when (viewType) {
    VIEW_TYPE_ITEM -> BaseViewHolder(
      CalendarMoviesItemView(parent.context).apply {
        itemClickListener = this@CalendarMoviesAdapter.itemClickListener
        itemLongClickListener = this@CalendarMoviesAdapter.itemLongClickListener
        missingImageListener = this@CalendarMoviesAdapter.missingImageListener
        missingTranslationListener = this@CalendarMoviesAdapter.missingTranslationListener
      },
    )
    VIEW_TYPE_HEADER -> BaseViewHolder(CalendarMoviesHeaderView(parent.context))
    VIEW_TYPE_FILTERS -> BaseViewHolder(
      CalendarMoviesFiltersView(parent.context).apply {
        onModeChipClick = this@CalendarMoviesAdapter.modeClickListener
      },
    )
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
  ) {
    when (val item = asyncDiffer.currentList[position]) {
      is CalendarMovieListItem.MovieItem -> (holder.itemView as CalendarMoviesItemView).bind(item)
      is CalendarMovieListItem.Header -> (holder.itemView as CalendarMoviesHeaderView).bind(item, position)
      is CalendarMovieListItem.Filters -> (holder.itemView as CalendarMoviesFiltersView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is CalendarMovieListItem.MovieItem -> VIEW_TYPE_ITEM
      is CalendarMovieListItem.Header -> VIEW_TYPE_HEADER
      is CalendarMovieListItem.Filters -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
