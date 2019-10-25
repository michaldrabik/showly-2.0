package com.michaldrabik.showly2.ui.show.related

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter

class RelatedShowAdapter : BaseAdapter<RelatedListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, RelatedItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(RelatedShowView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as RelatedShowView).bind(item, missingImageListener, itemClickListener)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}