package com.michaldrabik.ui_my_shows.views.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.NORMAL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersGridView
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersView
import com.michaldrabik.ui_my_shows.views.CollectionShowCompactView
import com.michaldrabik.ui_my_shows.views.CollectionShowGridTitleView
import com.michaldrabik.ui_my_shows.views.CollectionShowGridView
import com.michaldrabik.ui_my_shows.views.CollectionShowView

class CollectionAdapter(
  private val itemClickListener: (CollectionListItem) -> Unit,
  private val itemLongClickListener: (CollectionListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val upcomingChipClickListener: (Boolean) -> Unit,
  private val listViewChipClickListener: () -> Unit,
  private val missingImageListener: (CollectionListItem, Boolean) -> Unit,
  private val missingTranslationListener: (CollectionListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<CollectionListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, CollectionItemDiffCallback())

  var listViewMode: ListViewMode = NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_SHOW -> BaseMovieAdapter.BaseViewHolder(
        when (listViewMode) {
          NORMAL -> CollectionShowView(parent.context)
          COMPACT -> CollectionShowCompactView(parent.context)
          GRID -> CollectionShowGridView(parent.context)
          GRID_TITLE -> CollectionShowGridTitleView(parent.context)
        }.apply {
          itemClickListener = this@CollectionAdapter.itemClickListener
          itemLongClickListener = this@CollectionAdapter.itemLongClickListener
          missingImageListener = this@CollectionAdapter.missingImageListener
          missingTranslationListener = this@CollectionAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseMovieAdapter.BaseViewHolder(
        when (listViewMode) {
          NORMAL, COMPACT -> FollowedShowsFiltersView(parent.context).apply {
            onSortChipClicked = this@CollectionAdapter.sortChipClickListener
            onFilterUpcomingClicked = this@CollectionAdapter.upcomingChipClickListener
            onListViewModeClicked = this@CollectionAdapter.listViewChipClickListener
            isUpcomingChipVisible = true
          }
          GRID, GRID_TITLE -> FollowedShowsFiltersGridView(parent.context).apply {
            onSortChipClicked = this@CollectionAdapter.sortChipClickListener
            onFilterUpcomingClicked = this@CollectionAdapter.upcomingChipClickListener
            onListViewModeClicked = this@CollectionAdapter.listViewChipClickListener
            isUpcomingChipVisible = true
          }
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is CollectionListItem.FiltersItem ->
        when (listViewMode) {
          NORMAL, COMPACT ->
            (holder.itemView as FollowedShowsFiltersView).bind(item.sortOrder, item.sortType, item.isUpcoming)
          GRID ->
            (holder.itemView as FollowedShowsFiltersGridView).bind(item.sortOrder, item.sortType, item.isUpcoming)
          GRID_TITLE ->
            (holder.itemView as FollowedShowsFiltersGridView).bind(item.sortOrder, item.sortType, item.isUpcoming)
        }
      is CollectionListItem.ShowItem ->
        when (listViewMode) {
          NORMAL -> (holder.itemView as CollectionShowView).bind(item)
          COMPACT -> (holder.itemView as CollectionShowCompactView).bind(item)
          GRID -> (holder.itemView as CollectionShowGridView).bind(item)
          GRID_TITLE -> (holder.itemView as CollectionShowGridTitleView).bind(item)
        }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is CollectionListItem.ShowItem -> VIEW_TYPE_SHOW
      is CollectionListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
