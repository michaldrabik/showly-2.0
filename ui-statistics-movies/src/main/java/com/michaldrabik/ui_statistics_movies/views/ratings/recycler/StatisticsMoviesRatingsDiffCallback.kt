package com.michaldrabik.ui_statistics_movies.views.ratings.recycler

import androidx.recyclerview.widget.DiffUtil

class StatisticsMoviesRatingsDiffCallback : DiffUtil.ItemCallback<StatisticsMoviesRatingItem>() {

  override fun areItemsTheSame(oldItem: StatisticsMoviesRatingItem, newItem: StatisticsMoviesRatingItem) =
    oldItem.movie.ids.trakt == newItem.movie.ids.trakt

  override fun areContentsTheSame(oldItem: StatisticsMoviesRatingItem, newItem: StatisticsMoviesRatingItem) =
    oldItem.rating.rating == newItem.rating.rating &&
      oldItem.image == newItem.image
}
