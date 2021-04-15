package com.michaldrabik.ui_news.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_news.views.NewsItemView

class NewsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, NewsListItemDiffCallback())

  fun setItems(newItems: List<NewsListItem>) {
    asyncDiffer.submitList(newItems)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(
      NewsItemView(parent.context).apply {
//        itemClickListener = { super.itemClickListener.invoke(it) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as NewsItemView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
