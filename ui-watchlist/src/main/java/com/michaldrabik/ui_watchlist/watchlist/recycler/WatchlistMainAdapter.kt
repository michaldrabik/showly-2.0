package com.michaldrabik.ui_watchlist.watchlist.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_watchlist.WatchlistItem
import com.michaldrabik.ui_watchlist.WatchlistItemDiffCallback
import com.michaldrabik.ui_watchlist.watchlist.views.WatchlistMainHeaderView
import com.michaldrabik.ui_watchlist.watchlist.views.WatchlistMainItemView

class WatchlistMainAdapter : BaseAdapter<WatchlistItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  var detailsClickListener: ((WatchlistItem) -> Unit)? = null
  var checkClickListener: ((WatchlistItem) -> Unit)? = null
  var itemLongClickListener: ((WatchlistItem, View) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(WatchlistMainItemView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
        itemLongClickListener = { item, view ->
          this@WatchlistMainAdapter.itemLongClickListener?.invoke(item, view)
        }
        detailsClickListener = { this@WatchlistMainAdapter.detailsClickListener?.invoke(it) }
        checkClickListener = { this@WatchlistMainAdapter.checkClickListener?.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
      })
      VIEW_TYPE_HEADER -> BaseViewHolder(WatchlistMainHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as WatchlistMainHeaderView).bind(item.headerTextResId!!)
      VIEW_TYPE_ITEM -> (holder.itemView as WatchlistMainItemView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}
