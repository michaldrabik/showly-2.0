package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.ShowFanartView
import com.michaldrabik.showly2.ui.common.ShowPosterView
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem.Type.FANART

class DiscoverAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val TYPE_POSTER = 0
    private const val TYPE_FANART = 1
  }

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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when {
    viewType == TYPE_FANART -> ViewHolderWide(ShowFanartView(parent.context))
    viewType == TYPE_POSTER -> ViewHolder(ShowPosterView(parent.context))
    else -> throw IllegalStateException("Unknown view type.")
  }


  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      TYPE_FANART -> (holder.itemView as ShowFanartView).bind(items[position], missingImageListener)
      TYPE_POSTER -> (holder.itemView as ShowPosterView).bind(items[position], missingImageListener)
    }
  }

  override fun getItemCount() = items.size

  override fun getItemViewType(position: Int) = when {
    items[position].type == FANART -> TYPE_FANART
    else -> TYPE_POSTER
  }

  class ViewHolder(itemView: ShowPosterView) : RecyclerView.ViewHolder(itemView)

  class ViewHolderWide(itemView: ShowFanartView) : RecyclerView.ViewHolder(itemView)
}