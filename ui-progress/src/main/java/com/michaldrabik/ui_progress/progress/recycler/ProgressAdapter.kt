package com.michaldrabik.ui_progress.progress.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.progress.views.ProgressFiltersView
import com.michaldrabik.ui_progress.progress.views.ProgressHeaderView
import com.michaldrabik.ui_progress.progress.views.ProgressItemView

class ProgressAdapter(
  private val itemClickListener: (ProgressListItem) -> Unit,
  private val itemLongClickListener: (ProgressListItem) -> Unit,
  private val sortChipClickListener: () -> Unit,
  private val detailsClickListener: ((ProgressListItem.Episode) -> Unit)?,
  private val checkClickListener: ((ProgressListItem.Episode) -> Unit)?,
  private val headerClickListener: ((ProgressListItem.Header) -> Unit)?,
  private val missingImageListener: (ProgressListItem, Boolean) -> Unit,
  private val missingTranslationListener: (ProgressListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<ProgressListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_FILTERS = 3
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressItemView(parent.context).apply {
          itemClickListener = this@ProgressAdapter.itemClickListener
          itemLongClickListener = this@ProgressAdapter.itemLongClickListener
          missingImageListener = this@ProgressAdapter.missingImageListener
          missingTranslationListener = this@ProgressAdapter.missingTranslationListener
          checkClickListener = this@ProgressAdapter.checkClickListener
          detailsClickListener = this@ProgressAdapter.detailsClickListener
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(
        ProgressHeaderView(parent.context).apply {
          headerClickListener = this@ProgressAdapter.headerClickListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseViewHolder(
        ProgressFiltersView(parent.context).apply {
          onSortChipClicked = this@ProgressAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is ProgressListItem.Episode -> (holder.itemView as ProgressItemView).bind(item)
      is ProgressListItem.Header -> (holder.itemView as ProgressHeaderView).bind(item)
      is ProgressListItem.Filters -> (holder.itemView as ProgressFiltersView).bind(item.sortOrder, item.sortType)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is ProgressListItem.Header -> VIEW_TYPE_HEADER
      is ProgressListItem.Episode -> VIEW_TYPE_ITEM
      is ProgressListItem.Filters -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
