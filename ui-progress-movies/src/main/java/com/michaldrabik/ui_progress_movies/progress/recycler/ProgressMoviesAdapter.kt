package com.michaldrabik.ui_progress_movies.progress.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_progress_movies.progress.views.ProgressMoviesItemView

class ProgressMoviesAdapter(
  itemClickListener: (ProgressMovieListItem.MovieItem) -> Unit,
  missingImageListener: (ProgressMovieListItem.MovieItem, Boolean) -> Unit,
  missingTranslationListener: (ProgressMovieListItem.MovieItem) -> Unit,
  listChangeListener: () -> Unit,
  val checkClickListener: (ProgressMovieListItem.MovieItem) -> Unit,
  val itemLongClickListener: (ProgressMovieListItem.MovieItem, View) -> Unit,
) : BaseMovieAdapter<ProgressMovieListItem.MovieItem>(
  itemClickListener = itemClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener,
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, ProgressMovieItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      ProgressMoviesItemView(parent.context).apply {
        itemClickListener = this@ProgressMoviesAdapter.itemClickListener
        itemLongClickListener = this@ProgressMoviesAdapter.itemLongClickListener
        checkClickListener = this@ProgressMoviesAdapter.checkClickListener
        missingImageListener = this@ProgressMoviesAdapter.missingImageListener
        missingTranslationListener = this@ProgressMoviesAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ProgressMoviesItemView).bind(item)
  }
}
