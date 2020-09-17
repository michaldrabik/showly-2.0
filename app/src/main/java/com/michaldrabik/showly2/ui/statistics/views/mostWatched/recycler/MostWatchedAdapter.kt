package com.michaldrabik.showly2.ui.statistics.views.mostWatched.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.showly2.ui.statistics.views.mostWatched.StatisticsMostWatchedItemView

class MostWatchedAdapter : BaseAdapter<StatisticsMostWatchedItem>() {

  override val asyncDiffer = AsyncListDiffer(this, MostWatchedItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(StatisticsMostWatchedItemView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as StatisticsMostWatchedItemView).bind(item)
  }
}
