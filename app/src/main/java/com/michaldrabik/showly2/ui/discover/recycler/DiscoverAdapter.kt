package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.ui.common.ShowPosterView

class DiscoverAdapter : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {

  private val items: MutableList<Show> = mutableListOf()

  fun setItems(items: List<Show>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(ShowPosterView(parent.context))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    (holder.itemView as ShowPosterView).bind(items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolder(itemView: ShowPosterView) : RecyclerView.ViewHolder(itemView)
}