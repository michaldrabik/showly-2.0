package com.michaldrabik.ui_progress.history.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import com.michaldrabik.ui_progress.history.entities.HistoryListItem.Episode
import com.michaldrabik.ui_progress.history.entities.HistoryListItem.Filters
import com.michaldrabik.ui_progress.history.entities.HistoryListItem.Header
import com.michaldrabik.ui_progress.history.views.HistoryFiltersView
import com.michaldrabik.ui_progress.history.views.HistoryHeaderView
import com.michaldrabik.ui_progress.history.views.HistoryItemView

internal class HistoryAdapter(
  private val onItemClick: (HistoryListItem) -> Unit,
  private val onImageMissing: (HistoryListItem, Boolean) -> Unit,
  private val onTranslationMissing: (HistoryListItem) -> Unit,
  var onDetailsClick: ((Episode) -> Unit),
  var onDatesFilterClick: ((HistoryPeriod) -> Unit),
  listChangeListener: () -> Unit,
) : BaseAdapter<HistoryListItem>(listChangeListener) {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_FILTERS = 3
  }

  override val asyncDiffer = AsyncListDiffer(this, HistoryItemDiffCallback())

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ) = when (viewType) {
    VIEW_TYPE_ITEM -> BaseViewHolder(
      HistoryItemView(parent.context).apply {
        itemClickListener = this@HistoryAdapter.onItemClick
        missingImageListener = this@HistoryAdapter.onImageMissing
        missingTranslationListener = this@HistoryAdapter.onTranslationMissing
        onDetailsClick = this@HistoryAdapter.onDetailsClick
      },
    )
    VIEW_TYPE_HEADER -> BaseViewHolder(HistoryHeaderView(parent.context))
    VIEW_TYPE_FILTERS -> BaseViewHolder(
      HistoryFiltersView(parent.context).apply {
        onDatesChipClick = this@HistoryAdapter.onDatesFilterClick
      },
    )
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
  ) {
    when (val item = asyncDiffer.currentList[position]) {
      is Header -> (holder.itemView as HistoryHeaderView).bind(item, position)
      is Episode -> (holder.itemView as HistoryItemView).bind(item)
      is Filters -> (holder.itemView as HistoryFiltersView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is Header -> VIEW_TYPE_HEADER
      is Episode -> VIEW_TYPE_ITEM
      is Filters -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
