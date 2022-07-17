package com.michaldrabik.ui_news.recycler

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_news.views.item.NewsItemCardView
import com.michaldrabik.ui_news.views.item.NewsItemRowView
import com.michaldrabik.ui_news.views.item.NewsItemViewType
import com.michaldrabik.ui_news.views.item.NewsItemViewType.CARD
import com.michaldrabik.ui_news.views.item.NewsItemViewType.ROW

class NewsAdapter(
  val itemClickListener: (NewsListItem) -> Unit,
  val listChangeListener: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncListDiffer.ListListener<NewsListItem> {

  private val asyncDiffer = AsyncListDiffer(this, NewsListItemDiffCallback())
  private var viewType: NewsItemViewType = ROW

  @SuppressLint("NotifyDataSetChanged")
  fun setViewType(viewType: NewsItemViewType) {
    if (this.viewType != viewType) {
      this.viewType = viewType
      notifyDataSetChanged()
    }
  }

  fun setItems(
    newItems: List<NewsListItem>,
  ) {
    with(asyncDiffer) {
      removeListListener(this@NewsAdapter)
      addListListener(this@NewsAdapter)
      submitList(newItems)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = when (viewType) {
      ROW.ordinal -> NewsItemRowView(parent.context).apply {
        itemClickListener = { this@NewsAdapter.itemClickListener(it) }
      }
      CARD.ordinal -> NewsItemCardView(parent.context).apply {
        itemClickListener = { this@NewsAdapter.itemClickListener(it) }
      }
      else -> throw IllegalStateException()
    }
    return ViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      ROW.ordinal -> (holder.itemView as NewsItemRowView).bind(item)
      CARD.ordinal -> (holder.itemView as NewsItemCardView).bind(item)
    }
  }

  override fun getItemViewType(position: Int): Int = this.viewType.ordinal

  override fun getItemCount() = asyncDiffer.currentList.size

  override fun onCurrentListChanged(
    previousList: MutableList<NewsListItem>,
    currentList: MutableList<NewsListItem>,
  ) {
    listChangeListener()
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
