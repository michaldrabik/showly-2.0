package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.showly2.ui.common.ShowPosterView

class DiscoverAdapter : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {

  private val items: MutableList<DiscoverListItem> = mutableListOf()
  var missingImageListener: (Ids) -> Unit = {}

  fun setItems(items: List<DiscoverListItem>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  fun updateItemImageUrl(data: Pair<Ids, String>) {
    val target = items.find { it.show.ids.tvdb == data.first.tvdb }
    target?.let {
      target.imageUrl = data.second
      notifyItemChanged(items.indexOf(target))
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(ShowPosterView(parent.context))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    (holder.itemView as ShowPosterView).bind(items[position], missingImageListener)
  }

  override fun getItemCount() = items.size

  class ViewHolder(itemView: ShowPosterView) : RecyclerView.ViewHolder(itemView)
}