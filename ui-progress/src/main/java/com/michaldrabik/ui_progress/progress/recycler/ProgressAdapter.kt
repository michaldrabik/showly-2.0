package com.michaldrabik.ui_progress.progress.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.progress.views.ProgressHeaderView
import com.michaldrabik.ui_progress.progress.views.ProgressItemView

class ProgressAdapter(
  itemClickListener: (ProgressListItem) -> Unit,
  itemLongClickListener: (ProgressListItem) -> Unit,
  missingImageListener: (ProgressListItem, Boolean) -> Unit,
  missingTranslationListener: (ProgressListItem) -> Unit,
  listChangeListener: () -> Unit,
  var detailsClickListener: ((ProgressListItem.Episode) -> Unit)?,
  var checkClickListener: ((ProgressListItem.Episode) -> Unit)?,
  var headerClickListener: ((ProgressListItem.Header) -> Unit)?,
) : BaseAdapter<ProgressListItem>(
  itemClickListener = itemClickListener,
  itemLongClickListener = itemLongClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener,
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
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
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is ProgressListItem.Episode -> (holder.itemView as ProgressItemView).bind(item)
      is ProgressListItem.Header -> (holder.itemView as ProgressHeaderView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is ProgressListItem.Header -> VIEW_TYPE_HEADER
      is ProgressListItem.Episode -> VIEW_TYPE_ITEM
      else -> throw IllegalStateException()
    }
}
