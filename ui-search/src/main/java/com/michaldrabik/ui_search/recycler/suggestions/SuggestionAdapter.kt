package com.michaldrabik.ui_search.recycler.suggestions

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.views.SearchSuggestionView

class SuggestionAdapter(
  private val itemClickListener: (SearchListItem) -> Unit,
  private val missingImageListener: (SearchListItem, Boolean) -> Unit,
  private val missingTranslationListener: (SearchListItem) -> Unit
) : BaseAdapter<SearchListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, SuggestionItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      SearchSuggestionView(parent.context).apply {
        itemClickListener = this@SuggestionAdapter.itemClickListener
        missingImageListener = this@SuggestionAdapter.missingImageListener
        missingTranslationListener = this@SuggestionAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as SearchSuggestionView).bind(item)
  }
}
