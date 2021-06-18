package com.michaldrabik.ui_progress.progress.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.progress.views.ProgressHeaderView
import com.michaldrabik.ui_progress.progress.views.ProgressItemView

class ProgressAdapter : BaseAdapter<ProgressListItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressItemDiffCallback())

  var detailsClickListener: ((ProgressListItem.Episode) -> Unit)? = null
  var checkClickListener: ((ProgressListItem.Episode) -> Unit)? = null
  var itemLongClickListener: ((ProgressListItem.Episode, View) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          itemLongClickListener = { item, view ->
            this@ProgressAdapter.itemLongClickListener?.invoke(item, view)
          }
          detailsClickListener = { this@ProgressAdapter.detailsClickListener?.invoke(it) }
          checkClickListener = { this@ProgressAdapter.checkClickListener?.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
          missingTranslationListener = { super.missingTranslationListener.invoke(it) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(ProgressHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is ProgressListItem.Episode -> (holder.itemView as ProgressItemView).bind(item)
      is ProgressListItem.Header -> (holder.itemView as ProgressHeaderView).bind(item.textResId)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is ProgressListItem.Header -> VIEW_TYPE_HEADER
      is ProgressListItem.Episode -> VIEW_TYPE_ITEM
      else -> throw IllegalStateException()
    }
}
