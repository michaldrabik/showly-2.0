package com.michaldrabik.ui_my_shows.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowView

class WatchlistAdapter(
  private val itemClickListener: (WatchlistListItem) -> Unit,
  private val itemLongClickListener: (WatchlistListItem) -> Unit,
  private val missingImageListener: (WatchlistListItem, Boolean) -> Unit,
  private val missingTranslationListener: (WatchlistListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<WatchlistListItem>(
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      WatchlistShowView(parent.context).apply {
        itemClickListener = this@WatchlistAdapter.itemClickListener
        itemLongClickListener = this@WatchlistAdapter.itemLongClickListener
        missingImageListener = this@WatchlistAdapter.missingImageListener
        missingTranslationListener = this@WatchlistAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as WatchlistShowView).bind(item)
  }
}
