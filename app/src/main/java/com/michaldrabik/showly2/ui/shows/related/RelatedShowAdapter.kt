package com.michaldrabik.showly2.ui.shows.related

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter

class RelatedShowAdapter : BaseAdapter<RelatedListItem>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(RelatedShowView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as RelatedShowView).bind(items[position], missingImageListener, itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}