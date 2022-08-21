package com.michaldrabik.ui_progress_movies.progress.recycler

import androidx.recyclerview.widget.DiffUtil

class ProgressMovieItemDiffCallback : DiffUtil.ItemCallback<ProgressMovieListItem>() {

  override fun areItemsTheSame(oldItem: ProgressMovieListItem, newItem: ProgressMovieListItem): Boolean {
    val areMovies = oldItem is ProgressMovieListItem.MovieItem && newItem is ProgressMovieListItem.MovieItem
    val areFilters = oldItem is ProgressMovieListItem.FiltersItem && newItem is ProgressMovieListItem.FiltersItem

    return when {
      areMovies -> areItemsTheSame(
        (oldItem as ProgressMovieListItem.MovieItem),
        (newItem as ProgressMovieListItem.MovieItem)
      )
      areFilters -> true
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: ProgressMovieListItem, newItem: ProgressMovieListItem): Boolean {
    return when (oldItem) {
      is ProgressMovieListItem.MovieItem -> areContentsTheSame(oldItem, (newItem as ProgressMovieListItem.MovieItem))
      is ProgressMovieListItem.FiltersItem -> areContentsTheSame(oldItem, (newItem as ProgressMovieListItem.FiltersItem))
      is ProgressMovieListItem.Header -> true
    }
  }

  private fun areItemsTheSame(
    oldItem: ProgressMovieListItem.MovieItem,
    newItem: ProgressMovieListItem.MovieItem,
  ): Boolean {
    return oldItem.movie.ids.trakt == newItem.movie.ids.trakt
  }

  private fun areContentsTheSame(
    oldItem: ProgressMovieListItem.MovieItem,
    newItem: ProgressMovieListItem.MovieItem,
  ): Boolean {
    return oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image &&
      oldItem.movie == newItem.movie &&
      oldItem.sortOrder == newItem.sortOrder &&
      oldItem.userRating == newItem.userRating &&
      oldItem.translation == newItem.translation
  }

  private fun areContentsTheSame(
    oldItem: ProgressMovieListItem.FiltersItem,
    newItem: ProgressMovieListItem.FiltersItem,
  ): Boolean {
    return oldItem.sortOrder == newItem.sortOrder &&
      oldItem.sortType == newItem.sortType
  }
}
