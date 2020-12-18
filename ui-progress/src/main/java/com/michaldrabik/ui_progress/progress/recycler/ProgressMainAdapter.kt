package com.michaldrabik.ui_progress.progress.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.ProgressItemDiffCallback
import com.michaldrabik.ui_progress.progress.views.ProgressMainHeaderView
import com.michaldrabik.ui_progress.progress.views.ProgressMainItemView

class ProgressMainAdapter : BaseAdapter<ProgressItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressItemDiffCallback())

  var detailsClickListener: ((ProgressItem) -> Unit)? = null
  var checkClickListener: ((ProgressItem) -> Unit)? = null
  var itemLongClickListener: ((ProgressItem, View) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressMainItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          itemLongClickListener = { item, view ->
            this@ProgressMainAdapter.itemLongClickListener?.invoke(item, view)
          }
          detailsClickListener = { this@ProgressMainAdapter.detailsClickListener?.invoke(it) }
          checkClickListener = { this@ProgressMainAdapter.checkClickListener?.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
          missingTranslationListener = { super.missingTranslationListener.invoke(it) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(ProgressMainHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as ProgressMainHeaderView).bind(item.headerTextResId!!)
      VIEW_TYPE_ITEM -> (holder.itemView as ProgressMainItemView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}
