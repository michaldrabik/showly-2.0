package com.michaldrabik.ui_news.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_news.views.NewsItemView

class NewsAdapter(
  val itemClickListener: (NewsListItem) -> Unit,
  val listChangeListener: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncListDiffer.ListListener<NewsListItem> {

  private val asyncDiffer = AsyncListDiffer(this, NewsListItemDiffCallback())

  fun setItems(newItems: List<NewsListItem>) =
    with(asyncDiffer) {
      removeListListener(this@NewsAdapter)
      addListListener(this@NewsAdapter)
      submitList(newItems)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(
      NewsItemView(parent.context).apply {
        itemClickListener = { this@NewsAdapter.itemClickListener(it) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as NewsItemView).bind(item)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  override fun onCurrentListChanged(
    previousList: MutableList<NewsListItem>,
    currentList: MutableList<NewsListItem>,
  ) {
    listChangeListener()
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
