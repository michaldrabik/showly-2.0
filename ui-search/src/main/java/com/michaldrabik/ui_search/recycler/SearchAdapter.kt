package com.michaldrabik.ui_search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_search.views.ShowSearchView

class SearchAdapter : BaseAdapter<SearchListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, SearchItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(ShowSearchView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ShowSearchView).bind(item, missingImageListener)
  }
}
