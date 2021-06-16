package com.michaldrabik.ui_progress.calendar.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.calendar.views.CalendarHeaderView
import com.michaldrabik.ui_progress.calendar.views.CalendarItemView

class CalendarAdapter : BaseAdapter<CalendarListItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, CalendarItemDiffCallback())

  var detailsClickListener: ((CalendarListItem.Episode) -> Unit)? = null
  var checkClickListener: ((CalendarListItem.Episode) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        CalendarItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
          missingTranslationListener = { super.missingTranslationListener.invoke(it) }
          detailsClickListener = { this@CalendarAdapter.detailsClickListener?.invoke(it) }
          checkClickListener = { this@CalendarAdapter.checkClickListener?.invoke(it) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(CalendarHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is CalendarListItem.Episode -> (holder.itemView as CalendarItemView).bind(item)
      is CalendarListItem.Header -> (holder.itemView as CalendarHeaderView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is CalendarListItem.Header -> VIEW_TYPE_HEADER
      is CalendarListItem.Episode -> VIEW_TYPE_ITEM
      else -> throw IllegalStateException()
    }
}
