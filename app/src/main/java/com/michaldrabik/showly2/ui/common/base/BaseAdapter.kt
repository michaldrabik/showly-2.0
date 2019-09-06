package com.michaldrabik.showly2.ui.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

abstract class BaseAdapter<Item : ListItem> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  protected val items: MutableList<Item> = mutableListOf()
  var missingImageListener: (Item, Boolean) -> Unit = { _, _ -> }
  var itemClickListener: (Item) -> Unit = { }

  fun setItems(items: List<Item>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  fun updateItem(updatedItem: Item) {
    val target = items.find { it.show.ids == updatedItem.show.ids }
    target?.let {
      val index = items.indexOf(it)
      items.removeAt(index)
      items.add(index, updatedItem)
      notifyItemChanged(index)
    }
  }

  override fun getItemCount() = items.size

  fun indexOf(item: Item) = items.indexOf(item)

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}