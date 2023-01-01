package com.michaldrabik.ui_my_shows.common.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem.FiltersItem
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem.ShowItem
import com.michaldrabik.ui_my_shows.common.views.CollectionShowCompactView
import com.michaldrabik.ui_my_shows.common.views.CollectionShowFiltersView
import com.michaldrabik.ui_my_shows.common.views.CollectionShowGridTitleView
import com.michaldrabik.ui_my_shows.common.views.CollectionShowGridView
import com.michaldrabik.ui_my_shows.common.views.CollectionShowView

class CollectionAdapter(
  listChangeListener: () -> Unit,
  private val itemClickListener: (CollectionListItem) -> Unit,
  private val itemLongClickListener: (CollectionListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val upcomingChipClickListener: (Boolean) -> Unit,
  private val listViewChipClickListener: () -> Unit,
  private val missingImageListener: (CollectionListItem, Boolean) -> Unit,
  private val missingTranslationListener: (CollectionListItem) -> Unit,
  private val upcomingChipVisible: Boolean = true,
) : BaseAdapter<CollectionListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, CollectionItemDiffCallback())

  var listViewMode: ListViewMode = LIST_NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_SHOW -> BaseMovieAdapter.BaseViewHolder(
        when (listViewMode) {
          LIST_NORMAL -> CollectionShowView(parent.context)
          LIST_COMPACT -> CollectionShowCompactView(parent.context)
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
        CollectionShowFiltersView(parent.context).apply {
          onSortChipClicked = this@CollectionAdapter.sortChipClickListener
          onFilterUpcomingClicked = this@CollectionAdapter.upcomingChipClickListener
          onListViewModeClicked = this@CollectionAdapter.listViewChipClickListener
          isUpcomingChipVisible = upcomingChipVisible
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is FiltersItem ->
        (holder.itemView as CollectionShowFiltersView).bind(item, listViewMode)
      is ShowItem ->
        when (listViewMode) {
          LIST_NORMAL -> (holder.itemView as CollectionShowView).bind(item)
          LIST_COMPACT -> (holder.itemView as CollectionShowCompactView).bind(item)
          GRID -> (holder.itemView as CollectionShowGridView).bind(item)
          GRID_TITLE -> (holder.itemView as CollectionShowGridTitleView).bind(item)
        }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is ShowItem -> VIEW_TYPE_SHOW
      is FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
