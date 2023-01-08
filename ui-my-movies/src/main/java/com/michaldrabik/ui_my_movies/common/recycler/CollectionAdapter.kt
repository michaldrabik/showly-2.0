package com.michaldrabik.ui_my_movies.common.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem.FiltersItem
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem.MovieItem
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieCompactView
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieFiltersView
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieGridTitleView
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieGridView
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieView

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
) : BaseMovieAdapter<CollectionListItem>(
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
      VIEW_TYPE_SHOW -> BaseViewHolder(
        when (listViewMode) {
          LIST_NORMAL -> CollectionMovieView(parent.context)
          LIST_COMPACT -> CollectionMovieCompactView(parent.context)
          GRID -> CollectionMovieGridView(parent.context)
          GRID_TITLE -> CollectionMovieGridTitleView(parent.context)
        }.apply {
          itemClickListener = this@CollectionAdapter.itemClickListener
          itemLongClickListener = this@CollectionAdapter.itemLongClickListener
          missingImageListener = this@CollectionAdapter.missingImageListener
          missingTranslationListener = this@CollectionAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseViewHolder(
        CollectionMovieFiltersView(parent.context).apply {
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
        (holder.itemView as CollectionMovieFiltersView).bind(item, listViewMode)
      is MovieItem ->
        when (listViewMode) {
          LIST_NORMAL -> (holder.itemView as CollectionMovieView).bind(item)
          LIST_COMPACT -> (holder.itemView as CollectionMovieCompactView).bind(item)
          GRID -> (holder.itemView as CollectionMovieGridView).bind(item)
          GRID_TITLE -> (holder.itemView as CollectionMovieGridTitleView).bind(item)
        }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is MovieItem -> VIEW_TYPE_SHOW
      is FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
