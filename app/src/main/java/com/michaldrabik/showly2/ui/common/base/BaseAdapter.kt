package com.michaldrabik.showly2.ui.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

abstract class BaseAdapter<Item : ListItem> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  protected val items: MutableList<Item> = mutableListOf()
  var missingImageListener: (Item, Boolean) -> Unit = { _, _ -> }
  var itemClickListener: (Item) -> Unit = { }

  open fun setItems(newItems: List<Item>) {
    var isNew: Boolean
    this.items.apply {
      isNew = isEmpty()
      clear()
      addAll(newItems)
    }
    if (isNew) {
      notifyItemRangeInserted(0, newItems.size)
    } else {
      notifyItemRangeChanged(0, newItems.size)
    }
  }

  fun updateItem(updatedItem: Item) {
    val target = items.find { it.show.id == updatedItem.show.id }
    target?.let {
      val index = items.indexOf(it)
      items.removeAt(index)
      items.add(index, updatedItem)
      notifyItemChanged(index)
    }
  }

  fun clearItems() {
    items.clear()
    notifyDataSetChanged()
  }

  override fun getItemCount() = items.size

  fun indexOf(item: Item) = items.indexOf(item)

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}