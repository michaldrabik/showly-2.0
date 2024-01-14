package com.michaldrabik.ui_show.quicksetup

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_show.quicksetup.views.QuickSetupHeaderView
import com.michaldrabik.ui_show.quicksetup.views.QuickSetupItemView

class QuickSetupAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val TYPE_HEADER = 0
    private const val TYPE_ITEM = 1
  }

  var onItemClickListener: ((Episode, Boolean) -> Unit)? = null
  private val asyncDiffer = AsyncListDiffer(this, QuickSetupItemDiffCallback())

  fun setItems(newItems: List<QuickSetupListItem>) =
    asyncDiffer.submitList(newItems)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    TYPE_HEADER -> ViewHolder(QuickSetupHeaderView(parent.context))
    TYPE_ITEM -> ViewHolder(QuickSetupItemView(parent.context))
    else -> error("Unsupported view type")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      TYPE_HEADER -> (holder.itemView as QuickSetupHeaderView).bind(item.season)
      TYPE_ITEM -> (holder.itemView as QuickSetupItemView).bind(item.episode, item.isChecked, onItemClickListener)
    }
  }

  override fun getItemViewType(position: Int) = when {
    asyncDiffer.currentList[position].isHeader -> TYPE_HEADER
    else -> TYPE_ITEM
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  fun getItems() = asyncDiffer.currentList

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
