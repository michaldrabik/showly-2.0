package com.michaldrabik.ui_my_movies.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.filters.FollowedMoviesFiltersView
import com.michaldrabik.ui_my_movies.watchlist.views.WatchlistMovieView

class WatchlistAdapter(
  private val itemClickListener: (WatchlistListItem) -> Unit,
  private val itemLongClickListener: (WatchlistListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val missingImageListener: (WatchlistListItem, Boolean) -> Unit,
  private val missingTranslationListener: (WatchlistListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<WatchlistListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_MOVIE = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_MOVIE -> BaseViewHolder(
        WatchlistMovieView(parent.context).apply {
          itemClickListener = this@WatchlistAdapter.itemClickListener
          itemLongClickListener = this@WatchlistAdapter.itemLongClickListener
          missingImageListener = this@WatchlistAdapter.missingImageListener
          missingTranslationListener = this@WatchlistAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseViewHolder(
        FollowedMoviesFiltersView(parent.context).apply {
          onSortChipClicked = this@WatchlistAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is WatchlistListItem.FiltersItem ->
        (holder.itemView as FollowedMoviesFiltersView).bind(item.sortOrder, item.sortType)
      is WatchlistListItem.MovieItem ->
        (holder.itemView as WatchlistMovieView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is WatchlistListItem.MovieItem -> VIEW_TYPE_MOVIE
      is WatchlistListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
