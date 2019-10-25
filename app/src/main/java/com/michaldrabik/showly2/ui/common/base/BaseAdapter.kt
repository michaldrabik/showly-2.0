package com.michaldrabik.showly2.ui.common.base

import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

abstract class BaseAdapter<Item : ListItem> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  abstract val asyncDiffer: AsyncListDiffer<Item>

  var missingImageListener: (Item, Boolean) -> Unit = { _, _ -> }
  var itemClickListener: (Item) -> Unit = { }

  fun setItems(newItems: List<Item>) = asyncDiffer.submitList(newItems)

  fun updateItem(updatedItem: Item) {
    val target = asyncDiffer.currentList.find { it.show.id == updatedItem.show.id }
    target?.let {
      val index = asyncDiffer.currentList.indexOf(it)
      val newList = asyncDiffer.currentList.toMutableList()
      newList.removeAt(index)
      newList.add(index, updatedItem)
      setItems(newList)
    }
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  fun getItems(): List<Item> = asyncDiffer.currentList

  fun indexOf(item: Item) = asyncDiffer.currentList.indexOf(item)

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}