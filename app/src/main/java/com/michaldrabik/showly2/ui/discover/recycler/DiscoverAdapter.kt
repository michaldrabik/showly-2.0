package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.ShowPosterView

class DiscoverAdapter : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {

  private val items: MutableList<DiscoverListItem> = mutableListOf()
  var missingImageListener: (DiscoverListItem, Boolean) -> Unit = { _, _ -> }

  fun setItems(items: List<DiscoverListItem>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  fun updateItem(updatedItem: DiscoverListItem) {
    val target = items.find { it.show.ids == updatedItem.show.ids }
    val index = items.indexOf(target)
    items.removeAt(index)
    items.add(index, updatedItem)
    notifyItemChanged(index)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(ShowPosterView(parent.context))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    (holder.itemView as ShowPosterView).bind(items[position], missingImageListener)
  }

  override fun getItemCount() = items.size

  class ViewHolder(itemView: ShowPosterView) : RecyclerView.ViewHolder(itemView)
}