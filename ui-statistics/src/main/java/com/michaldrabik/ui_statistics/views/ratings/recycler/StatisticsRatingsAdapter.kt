package com.michaldrabik.ui_statistics.views.ratings.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_statistics.views.ratings.StatisticsRateItemView

class StatisticsRatingsAdapter(
  private val itemClickListener: (StatisticsRatingItem) -> Unit,
) : BaseAdapter<StatisticsRatingItem>() {

  override val asyncDiffer = AsyncListDiffer(this, StatisticsRatingsDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      StatisticsRateItemView(parent.context).apply {
        itemClickListener = this@StatisticsRatingsAdapter.itemClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as StatisticsRateItemView).bind(item)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
