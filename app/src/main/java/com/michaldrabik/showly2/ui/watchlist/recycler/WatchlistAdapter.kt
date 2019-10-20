package com.michaldrabik.showly2.ui.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistItemView

class WatchlistAdapter : BaseAdapter<WatchlistItem>() {

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
    ViewHolderShow(WatchlistItemView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as WatchlistItemView).bind(
      items[position],
      itemClickListener,
      detailsClickListener,
      checkClickListener,
      missingImageListener
    )
  }
}