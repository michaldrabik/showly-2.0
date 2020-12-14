package com.michaldrabik.ui_progress_movies.progress.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.ProgressMovieItemDiffCallback
import com.michaldrabik.ui_progress_movies.progress.views.ProgressMoviesMainItemView

class ProgressMainAdapter : BaseMovieAdapter<ProgressMovieItem>() {

  override val asyncDiffer = AsyncListDiffer(this, ProgressMovieItemDiffCallback())

  var checkClickListener: ((ProgressMovieItem) -> Unit)? = null
  var itemLongClickListener: ((ProgressMovieItem, View) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      ProgressMoviesMainItemView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
        itemLongClickListener = { item, view ->
          this@ProgressMainAdapter.itemLongClickListener?.invoke(item, view)
        }
        checkClickListener = { this@ProgressMainAdapter.checkClickListener?.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ProgressMoviesMainItemView).bind(item)
  }
}
