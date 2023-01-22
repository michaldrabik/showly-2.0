package com.michaldrabik.ui_my_movies.mymovies.recycler

import androidx.recyclerview.widget.DiffUtil

class MyMoviesItemDiffCallback : DiffUtil.ItemCallback<MyMoviesItem>() {

  override fun areItemsTheSame(oldItem: MyMoviesItem, newItem: MyMoviesItem) =
    when (oldItem.type) {
      MyMoviesItem.Type.RECENT_MOVIES -> true
      else -> oldItem.type == newItem.type && oldItem.movie.ids.trakt == newItem.movie.ids.trakt
    }

  override fun areContentsTheSame(oldItem: MyMoviesItem, newItem: MyMoviesItem) =
    when (oldItem.type) {
      MyMoviesItem.Type.HEADER -> oldItem.header == newItem.header
      MyMoviesItem.Type.RECENT_MOVIES -> oldItem.recentsSection == newItem.recentsSection
      else ->
        oldItem.image == newItem.image &&
          oldItem.isLoading == newItem.isLoading &&
          oldItem.translation == newItem.translation &&
          oldItem.userRating == newItem.userRating
    }
}
