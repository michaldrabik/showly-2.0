package com.michaldrabik.ui_my_movies.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_my_movies.watchlist.views.WatchlistMovieView

class WatchlistAdapter : BaseMovieAdapter<WatchlistListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      WatchlistMovieView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as WatchlistMovieView).bind(item, missingImageListener)
  }
}
