package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.FANART_WIDE
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.views.ShowFanartView
import com.michaldrabik.showly2.ui.common.views.ShowPosterView
import com.michaldrabik.showly2.ui.common.views.ShowView

class DiscoverAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<DiscoverListItem> = mutableListOf()
  var missingImageListener: (DiscoverListItem, Boolean) -> Unit = { _, _ -> }
  var itemClickListener: (DiscoverListItem) -> Unit = { }

  fun setItems(items: List<DiscoverListItem>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  fun updateItem(updatedItem: DiscoverListItem) {
    val target = items.find { it.show.ids == updatedItem.show.ids }
    target?.let {
      val index = items.indexOf(it)
      items.removeAt(index)
      items.add(index, updatedItem)
      notifyItemChanged(index)
    }
  }

  fun findItemIndex(item: DiscoverListItem) = items.indexOf(item)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    POSTER.id -> ViewHolderShow(ShowPosterView(parent.context))
    FANART.id, FANART_WIDE.id -> ViewHolderShow(ShowFanartView(parent.context))
    else -> throw IllegalStateException("Unknown view type.")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      POSTER.id ->
        (holder.itemView as ShowView).bind(items[position], missingImageListener, itemClickListener)
      FANART.id, FANART_WIDE.id ->
        (holder.itemView as ShowView).bind(items[position], missingImageListener, itemClickListener)
    }
  }

  override fun getItemCount() = items.size

  override fun getItemViewType(position: Int) = items[position].image.type.id

  class ViewHolderShow(itemView: ShowView) : RecyclerView.ViewHolder(itemView)
}