package com.michaldrabik.showly2.ui.search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.search.views.ShowSearchView

class SearchAdapter : BaseAdapter<SearchListItem>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(ShowSearchView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as ShowSearchView).bind(items[position], missingImageListener, itemClickListener)
  }
}