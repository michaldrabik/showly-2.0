package com.michaldrabik.ui_streamings.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_streamings.views.StreamingView

class StreamingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, StreamingItemDiffCallback())

  fun setItems(newItems: List<StreamingService>) {
    with(asyncDiffer) {
      submitList(newItems)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(StreamingView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as StreamingView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
