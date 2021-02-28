package com.michaldrabik.ui_my_shows.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowView

class WatchlistAdapter : BaseAdapter<WatchlistListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      WatchlistShowView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
        missingTranslationListener = { super.missingTranslationListener.invoke(it) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as WatchlistShowView).bind(item)
  }
}
