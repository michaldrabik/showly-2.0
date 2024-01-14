package com.michaldrabik.ui_search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_search.views.SearchItemView

class SearchAdapter(
  private val itemClickListener: (SearchListItem) -> Unit,
  private val itemLongClickListener: (SearchListItem) -> Unit,
  private val missingImageListener: (SearchListItem, Boolean) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<SearchListItem>(
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, SearchItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      SearchItemView(parent.context).apply {
        itemClickListener = this@SearchAdapter.itemClickListener
        itemLongClickListener = this@SearchAdapter.itemLongClickListener
        missingImageListener = this@SearchAdapter.missingImageListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as SearchItemView).bind(item)
  }
}
