package com.michaldrabik.ui_progress_movies.progress.recycler

import androidx.recyclerview.widget.DiffUtil

class ProgressMovieItemDiffCallback : DiffUtil.ItemCallback<ProgressMovieListItem.MovieItem>() {

  override fun areItemsTheSame(oldItem: ProgressMovieListItem.MovieItem, newItem: ProgressMovieListItem.MovieItem) =
    oldItem.movie.ids.trakt == newItem.movie.ids.trakt

  override fun areContentsTheSame(oldItem: ProgressMovieListItem.MovieItem, newItem: ProgressMovieListItem.MovieItem) =
    oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image &&
      oldItem.movie == newItem.movie &&
      oldItem.translation == newItem.translation
}
