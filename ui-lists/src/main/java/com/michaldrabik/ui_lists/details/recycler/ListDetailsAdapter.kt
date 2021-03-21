package com.michaldrabik.ui_lists.details.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_lists.lists.recycler.ListsItemDiffCallback
import com.michaldrabik.ui_lists.lists.views.ListsItemView

class ListDetailsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncListDiffer.ListListener<ListsItem> {

  private val asyncDiffer = AsyncListDiffer(this, ListsItemDiffCallback())

  var itemsChangedListener: (() -> Unit)? = null
  private var notifyItemsChange = false

  init {
    asyncDiffer.addListListener(this)
  }

  fun setItems(newItems: List<ListsItem>, notifyItemsChange: Boolean = false) {
    this.notifyItemsChange = notifyItemsChange
    asyncDiffer.submitList(newItems)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ListsItemViewHolder(ListsItemView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ListsItemView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  override fun onCurrentListChanged(oldList: MutableList<ListsItem>, newList: MutableList<ListsItem>) {
    if (notifyItemsChange) itemsChangedListener?.invoke()
  }

  class ListsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}