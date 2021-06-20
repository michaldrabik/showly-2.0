package com.michaldrabik.ui_progress_movies.calendar.recycler

import androidx.recyclerview.widget.DiffUtil

class CalendarMovieItemDiffCallback : DiffUtil.ItemCallback<CalendarMovieListItem>() {

  override fun areItemsTheSame(oldItem: CalendarMovieListItem, newItem: CalendarMovieListItem): Boolean {
    if (oldItem is CalendarMovieListItem.Header && newItem is CalendarMovieListItem.Header) {
      return oldItem.textResId == newItem.textResId
    }
    if (oldItem is CalendarMovieListItem.MovieItem && newItem is CalendarMovieListItem.MovieItem) {
      return oldItem.movie.traktId == newItem.movie.traktId
    }
    return false
  }

  override fun areContentsTheSame(oldItem: CalendarMovieListItem, newItem: CalendarMovieListItem): Boolean {
    if (oldItem is CalendarMovieListItem.Header && newItem is CalendarMovieListItem.Header) {
      return oldItem.textResId == newItem.textResId
    }
    if (oldItem is CalendarMovieListItem.MovieItem && newItem is CalendarMovieListItem.MovieItem) {
      return oldItem.image == newItem.image &&
        oldItem.isWatched == newItem.isWatched &&
        oldItem.isLoading == newItem.isLoading &&
        oldItem.translation == newItem.translation &&
        oldItem.movie == newItem.movie
    }
    return false
  }
}
