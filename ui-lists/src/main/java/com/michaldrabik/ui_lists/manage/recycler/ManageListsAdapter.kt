package com.michaldrabik.ui_lists.manage.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_lists.manage.views.ManageListsItemView

class ManageListsAdapter(
  val itemCheckListener: ((ManageListsItem, Boolean) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, ManageListsItemDiffCallback())

  fun setItems(newItems: List<ManageListsItem>) {
    asyncDiffer.submitList(newItems)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ManageListsItemViewHolder(
      ManageListsItemView(parent.context).apply {
        itemCheckListener = { item, isChecked ->
          this@ManageListsAdapter.itemCheckListener.invoke(item, isChecked)
        }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ManageListsItemView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class ManageListsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
