package com.michaldrabik.showly2.ui.search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.search.views.ShowSearchView

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<SearchListItem> = mutableListOf()
  var missingImageListener: (SearchListItem, Boolean) -> Unit = { _, _ -> }
  var itemClickListener: (SearchListItem) -> Unit = { }

  fun setItems(items: List<SearchListItem>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  fun updateItem(updatedItem: SearchListItem) {
    val target = items.find { it.show.ids == updatedItem.show.ids }
    target?.let {
      val index = items.indexOf(it)
      items.removeAt(index)
      items.add(index, updatedItem)
      notifyItemChanged(index)
    }
  }

  fun findItemIndex(item: SearchListItem) = items.indexOf(item)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(ShowSearchView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as ShowSearchView).bind(items[position], missingImageListener, itemClickListener)
  }

  override fun getItemCount() = items.size

  override fun getItemViewType(position: Int) = items[position].image.type.id

  class ViewHolderShow(itemView: ShowSearchView) : RecyclerView.ViewHolder(itemView)
}