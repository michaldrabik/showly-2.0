package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.ImageType.FANART
import com.michaldrabik.showly2.ui.common.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.views.ShowFanartView
import com.michaldrabik.showly2.ui.common.views.ShowPosterView

class DiscoverAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    POSTER.ordinal -> ViewHolderPoster(ShowPosterView(parent.context))
    FANART.ordinal -> ViewHolderFanart(ShowFanartView(parent.context))
    else -> throw IllegalStateException("Unknown view type.")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      POSTER.ordinal -> (holder.itemView as ShowPosterView).bind(items[position], missingImageListener)
      FANART.ordinal -> (holder.itemView as ShowFanartView).bind(items[position], missingImageListener)
    }
  }

  override fun getItemCount() = items.size

  override fun getItemViewType(position: Int) = items[position].type.ordinal

  class ViewHolderPoster(itemView: ShowPosterView) : RecyclerView.ViewHolder(itemView)

  class ViewHolderFanart(itemView: ShowFanartView) : RecyclerView.ViewHolder(itemView)
}