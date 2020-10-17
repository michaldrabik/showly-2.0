package com.michaldrabik.ui_progress.calendar.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.ProgressItemDiffCallback
import com.michaldrabik.ui_progress.calendar.views.ProgressCalendarHeaderView
import com.michaldrabik.ui_progress.calendar.views.ProgressCalendarItemView

class ProgressCalendarAdapter : BaseAdapter<ProgressItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressItemDiffCallback())

  var detailsClickListener: ((ProgressItem) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressCalendarItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          detailsClickListener = { this@ProgressCalendarAdapter.detailsClickListener?.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(ProgressCalendarHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as ProgressCalendarHeaderView).bind(item.headerTextResId!!)
      VIEW_TYPE_ITEM -> (holder.itemView as ProgressCalendarItemView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}
