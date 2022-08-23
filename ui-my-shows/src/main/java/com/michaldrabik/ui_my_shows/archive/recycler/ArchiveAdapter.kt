package com.michaldrabik.ui_my_shows.archive.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.archive.recycler.views.ArchiveShowView
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersView

class ArchiveAdapter(
  private val itemClickListener: (ArchiveListItem) -> Unit,
  private val itemLongClickListener: (ArchiveListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val missingImageListener: (ArchiveListItem, Boolean) -> Unit,
  private val missingTranslationListener: (ArchiveListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<ArchiveListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ArchiveDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_SHOW -> BaseMovieAdapter.BaseViewHolder(
        ArchiveShowView(parent.context).apply {
          itemClickListener = this@ArchiveAdapter.itemClickListener
          itemLongClickListener = this@ArchiveAdapter.itemLongClickListener
          missingImageListener = this@ArchiveAdapter.missingImageListener
          missingTranslationListener = this@ArchiveAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseMovieAdapter.BaseViewHolder(
        FollowedShowsFiltersView(parent.context).apply {
          onSortChipClicked = this@ArchiveAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is ArchiveListItem.FiltersItem ->
        (holder.itemView as FollowedShowsFiltersView).bind(item.sortOrder, item.sortType)
      is ArchiveListItem.ShowItem ->
        (holder.itemView as ArchiveShowView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is ArchiveListItem.ShowItem -> VIEW_TYPE_SHOW
      is ArchiveListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
