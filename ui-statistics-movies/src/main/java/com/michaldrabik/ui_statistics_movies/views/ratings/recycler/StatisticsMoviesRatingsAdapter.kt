package com.michaldrabik.ui_statistics_movies.views.ratings.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_statistics_movies.views.ratings.StatisticsMoviesRateItemView

class StatisticsMoviesRatingsAdapter(
  itemClickListener: (StatisticsMoviesRatingItem) -> Unit
) : BaseMovieAdapter<StatisticsMoviesRatingItem>(
  itemClickListener = itemClickListener,
) {

  override val asyncDiffer = AsyncListDiffer(this, StatisticsMoviesRatingsDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      StatisticsMoviesRateItemView(parent.context).apply {
        itemClickListener = this@StatisticsMoviesRatingsAdapter.itemClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as StatisticsMoviesRateItemView).bind(item)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
