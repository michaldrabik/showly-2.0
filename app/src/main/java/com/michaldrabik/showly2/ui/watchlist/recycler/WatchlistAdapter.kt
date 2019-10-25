package com.michaldrabik.showly2.ui.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistHeaderView
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistItemView

class WatchlistAdapter : BaseAdapter<WatchlistItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  var detailsClickListener: (WatchlistItem) -> Unit = { }
  var checkClickListener: (WatchlistItem) -> Unit = { }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(WatchlistItemView(parent.context))
      VIEW_TYPE_HEADER -> BaseViewHolder(WatchlistHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as WatchlistHeaderView).bind(
        item.headerTextResId!!
      )
      VIEW_TYPE_ITEM -> (holder.itemView as WatchlistItemView).bind(
        item,
        itemClickListener,
        detailsClickListener,
        checkClickListener,
        missingImageListener
      )
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}