package com.michaldrabik.ui_search.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_search.views.ShowSearchView

class SearchAdapter : BaseAdapter<SearchListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, SearchItemDiffCallback())

  override fun setItems(newItems: List<SearchListItem>, notifyChange: Boolean) {
    FirebaseCrashlytics.getInstance().setCustomKey("Adapter", "SearchAdapter")
    super.setItems(newItems, notifyChange)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      ShowSearchView(parent.context).apply {
        itemClickListener = { super.itemClickListener.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ShowSearchView).bind(item)
  }
}
