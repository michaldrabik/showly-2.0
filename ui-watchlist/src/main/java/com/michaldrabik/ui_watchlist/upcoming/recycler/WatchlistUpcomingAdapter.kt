package com.michaldrabik.ui_watchlist.upcoming.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_watchlist.WatchlistItem
import com.michaldrabik.ui_watchlist.WatchlistItemDiffCallback
import com.michaldrabik.ui_watchlist.upcoming.views.WatchlistUpcomingHeaderView
import com.michaldrabik.ui_watchlist.upcoming.views.WatchlistUpcomingItemView

class WatchlistUpcomingAdapter : BaseAdapter<WatchlistItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  var detailsClickListener: ((WatchlistItem) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(WatchlistUpcomingItemView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
        detailsClickListener = { this@WatchlistUpcomingAdapter.detailsClickListener?.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
      })
      VIEW_TYPE_HEADER -> BaseViewHolder(WatchlistUpcomingHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as WatchlistUpcomingHeaderView).bind(item.headerTextResId!!)
      VIEW_TYPE_ITEM -> (holder.itemView as WatchlistUpcomingItemView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}
