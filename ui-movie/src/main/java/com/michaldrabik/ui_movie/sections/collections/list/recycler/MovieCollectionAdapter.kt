package com.michaldrabik.ui_movie.sections.collections.list.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.MovieCollection

class MovieCollectionAdapter(
  private val itemClickListener: (MovieCollection) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, MovieCollectionDiffCallback())

  fun setItems(newItems: List<MovieCollection>) {
    with(asyncDiffer) {
      submitList(newItems)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = MovieCollectionItemView(parent.context).apply {
      itemClickListener = this@MovieCollectionAdapter.itemClickListener
    }
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as MovieCollectionItemView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
