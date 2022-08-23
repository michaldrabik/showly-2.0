package com.michaldrabik.ui_my_shows.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersView
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowView

class WatchlistAdapter(
  private val itemClickListener: (WatchlistListItem) -> Unit,
  private val itemLongClickListener: (WatchlistListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val missingImageListener: (WatchlistListItem, Boolean) -> Unit,
  private val missingTranslationListener: (WatchlistListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<WatchlistListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_SHOW -> BaseMovieAdapter.BaseViewHolder(
        WatchlistShowView(parent.context).apply {
          itemClickListener = this@WatchlistAdapter.itemClickListener
          itemLongClickListener = this@WatchlistAdapter.itemLongClickListener
          missingImageListener = this@WatchlistAdapter.missingImageListener
          missingTranslationListener = this@WatchlistAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseMovieAdapter.BaseViewHolder(
        FollowedShowsFiltersView(parent.context).apply {
          onSortChipClicked = this@WatchlistAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is WatchlistListItem.FiltersItem ->
        (holder.itemView as FollowedShowsFiltersView).bind(item.sortOrder, item.sortType)
      is WatchlistListItem.ShowItem ->
        (holder.itemView as WatchlistShowView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is WatchlistListItem.ShowItem -> VIEW_TYPE_SHOW
      is WatchlistListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
