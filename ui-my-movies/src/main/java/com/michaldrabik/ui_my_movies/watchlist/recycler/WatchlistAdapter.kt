package com.michaldrabik.ui_my_movies.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_my_movies.watchlist.views.WatchlistMovieView

class WatchlistAdapter(
  itemClickListener: (WatchlistListItem) -> Unit,
  missingImageListener: (WatchlistListItem, Boolean) -> Unit,
  missingTranslationListener: (WatchlistListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<WatchlistListItem>(
  itemClickListener = itemClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener,
  listChangeListener = listChangeListener
) {

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      WatchlistMovieView(parent.context).apply {
        itemClickListener = this@WatchlistAdapter.itemClickListener
        missingImageListener = this@WatchlistAdapter.missingImageListener
        missingTranslationListener = this@WatchlistAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as WatchlistMovieView).bind(item)
  }
}
