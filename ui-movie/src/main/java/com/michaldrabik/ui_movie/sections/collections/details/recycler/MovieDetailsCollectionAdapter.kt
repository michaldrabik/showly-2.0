package com.michaldrabik.ui_movie.sections.collections.details.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.HeaderItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.LoadingItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.MovieItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.views.MovieDetailsCollectionHeaderView
import com.michaldrabik.ui_movie.sections.collections.details.recycler.views.MovieDetailsCollectionItemView
import com.michaldrabik.ui_movie.sections.collections.details.recycler.views.MovieDetailsCollectionLoadingView

class MovieDetailsCollectionAdapter(
  var onItemClickListener: (MovieDetailsCollectionItem) -> Unit,
  var onItemLongClickListener: (MovieDetailsCollectionItem) -> Unit,
  val onMissingImageListener: (MovieDetailsCollectionItem, Boolean) -> Unit,
  val onMissingTranslationListener: (MovieDetailsCollectionItem) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_LOADING = 3
  }

  private val asyncDiffer = AsyncListDiffer(this, MovieDetailsCollectionItemDiffCallback())

  fun setItems(items: List<MovieDetailsCollectionItem>) = asyncDiffer.submitList(items)

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is HeaderItem -> VIEW_TYPE_HEADER
      is MovieItem -> VIEW_TYPE_ITEM
      is LoadingItem -> VIEW_TYPE_LOADING
      else -> throw IllegalStateException()
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        MovieDetailsCollectionItemView(parent.context).apply {
          onItemClickListener = this@MovieDetailsCollectionAdapter.onItemClickListener
          onItemLongClickListener = this@MovieDetailsCollectionAdapter.onItemLongClickListener
          onMissingImageListener = this@MovieDetailsCollectionAdapter.onMissingImageListener
          onMissingTranslationListener = this@MovieDetailsCollectionAdapter.onMissingTranslationListener
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(MovieDetailsCollectionHeaderView(parent.context))
      VIEW_TYPE_LOADING -> BaseViewHolder(MovieDetailsCollectionLoadingView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (val item = asyncDiffer.currentList[position]) {
      is HeaderItem -> (holder.itemView as MovieDetailsCollectionHeaderView).bind(item)
      is MovieItem -> (holder.itemView as MovieDetailsCollectionItemView).bind(item)
      is LoadingItem -> Unit
    }

  override fun getItemCount() = asyncDiffer.currentList.size

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
