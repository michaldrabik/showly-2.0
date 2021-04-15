package com.michaldrabik.ui_news.recycler

import androidx.recyclerview.widget.DiffUtil

class NewsListItemDiffCallback : DiffUtil.ItemCallback<NewsListItem>() {

  override fun areItemsTheSame(
    oldItem: NewsListItem,
    newItem: NewsListItem,
  ) =
    oldItem.item.id == newItem.item.id &&
      oldItem.item.type == newItem.item.type

  override fun areContentsTheSame(
    oldItem: NewsListItem,
    newItem: NewsListItem,
  ) =
    oldItem.item == newItem.item
}
