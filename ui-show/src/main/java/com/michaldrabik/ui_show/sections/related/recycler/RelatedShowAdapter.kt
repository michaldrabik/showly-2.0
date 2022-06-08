package com.michaldrabik.ui_show.sections.related.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter

class RelatedShowAdapter(
  private val itemClickListener: (RelatedListItem) -> Unit,
  private val missingImageListener: (RelatedListItem, Boolean) -> Unit,
) : BaseAdapter<RelatedListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, RelatedItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      RelatedShowView(parent.context).apply {
        itemClickListener = this@RelatedShowAdapter.itemClickListener
        missingImageListener = this@RelatedShowAdapter.missingImageListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as RelatedShowView).bind(item)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
