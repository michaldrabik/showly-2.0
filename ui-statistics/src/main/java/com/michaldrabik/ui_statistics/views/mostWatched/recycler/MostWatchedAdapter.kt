package com.michaldrabik.ui_statistics.views.mostWatched.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItemView

class MostWatchedAdapter(
  private val itemClickListener: (StatisticsMostWatchedItem) -> Unit,
) : BaseAdapter<StatisticsMostWatchedItem>() {

  override val asyncDiffer = AsyncListDiffer(this, MostWatchedItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      StatisticsMostWatchedItemView(parent.context).apply {
        itemClickListener = this@MostWatchedAdapter.itemClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as StatisticsMostWatchedItemView).bind(item)
  }
}
