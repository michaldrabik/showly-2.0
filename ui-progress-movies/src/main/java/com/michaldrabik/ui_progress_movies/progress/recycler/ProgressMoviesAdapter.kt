package com.michaldrabik.ui_progress_movies.progress.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress_movies.progress.views.ProgressMoviesFiltersView
import com.michaldrabik.ui_progress_movies.progress.views.ProgressMoviesItemView

class ProgressMoviesAdapter(
  private val itemClickListener: (ProgressMovieListItem.MovieItem) -> Unit,
  private val itemLongClickListener: (ProgressMovieListItem.MovieItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val missingImageListener: (ProgressMovieListItem.MovieItem, Boolean) -> Unit,
  private val missingTranslationListener: (ProgressMovieListItem.MovieItem) -> Unit,
  private val checkClickListener: (ProgressMovieListItem.MovieItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<ProgressMovieListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_MOVIE = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressMovieItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return when (viewType) {
      VIEW_TYPE_MOVIE -> BaseViewHolder(
        ProgressMoviesItemView(parent.context).apply {
          itemClickListener = this@ProgressMoviesAdapter.itemClickListener
          itemLongClickListener = this@ProgressMoviesAdapter.itemLongClickListener
          checkClickListener = this@ProgressMoviesAdapter.checkClickListener
          missingImageListener = this@ProgressMoviesAdapter.missingImageListener
          missingTranslationListener = this@ProgressMoviesAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseViewHolder(
        ProgressMoviesFiltersView(parent.context).apply {
          onSortChipClicked = this@ProgressMoviesAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is ProgressMovieListItem.FiltersItem ->
        (holder.itemView as ProgressMoviesFiltersView).bind(item.sortOrder, item.sortType)
      is ProgressMovieListItem.MovieItem ->
        (holder.itemView as ProgressMoviesItemView).bind(item)
      else -> throw IllegalStateException()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (asyncDiffer.currentList[position]) {
      is ProgressMovieListItem.MovieItem -> VIEW_TYPE_MOVIE
      is ProgressMovieListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
  }
}
