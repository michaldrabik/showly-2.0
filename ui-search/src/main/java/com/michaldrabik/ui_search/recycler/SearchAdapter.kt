package com.michaldrabik.showly2.ui.search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.search.views.ShowSearchView
import com.michaldrabik.ui_base.BaseAdapter

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
