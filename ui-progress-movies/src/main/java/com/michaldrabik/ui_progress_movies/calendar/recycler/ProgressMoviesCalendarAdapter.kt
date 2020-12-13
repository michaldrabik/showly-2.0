package com.michaldrabik.ui_progress_movies.calendar.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.ProgressMovieItemDiffCallback
import com.michaldrabik.ui_progress_movies.calendar.views.ProgressMoviesCalendarHeaderView
import com.michaldrabik.ui_progress_movies.calendar.views.ProgressMoviesCalendarItemView

class ProgressMoviesCalendarAdapter : BaseMovieAdapter<ProgressMovieItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, ProgressMovieItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressMoviesCalendarItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(ProgressMoviesCalendarHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as ProgressMoviesCalendarHeaderView).bind(item.headerTextResId!!)
      VIEW_TYPE_ITEM -> (holder.itemView as ProgressMoviesCalendarItemView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when {
      asyncDiffer.currentList[position].isHeader() -> VIEW_TYPE_HEADER
      else -> VIEW_TYPE_ITEM
    }
}
