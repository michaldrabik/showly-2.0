package com.michaldrabik.showly2.ui.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistHeaderView
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistItemView

class WatchlistAdapter : BaseAdapter<WatchlistItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  var detailsClickListener: (WatchlistItem) -> Unit = { }
  var checkClickListener: (WatchlistItem) -> Unit = { }

  override fun setItems(newItems: List<WatchlistItem>) {
    val diffCallback = WatchlistItemDiffCallback(items, newItems)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    this.items.apply {
      clear()
      addAll(newItems)
    }
    diffResult.dispatchUpdatesTo(this)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> ViewHolderShow(WatchlistItemView(parent.context))
      VIEW_TYPE_HEADER -> ViewHolderShow(WatchlistHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as WatchlistHeaderView).bind(
        items[position].headerTextResId!!
      )
      VIEW_TYPE_ITEM -> (holder.itemView as WatchlistItemView).bind(
        items[position],
        itemClickListener,
        detailsClickListener,
        checkClickListener,
        missingImageListener
      )
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      items[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}