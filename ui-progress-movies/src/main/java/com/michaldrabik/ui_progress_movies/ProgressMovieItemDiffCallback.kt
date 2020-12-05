package com.michaldrabik.ui_progress_movies

import androidx.recyclerview.widget.DiffUtil

class ProgressMovieItemDiffCallback : DiffUtil.ItemCallback<ProgressMovieItem>() {

  override fun areItemsTheSame(oldItem: ProgressMovieItem, newItem: ProgressMovieItem) =
    oldItem.movie.ids.trakt == newItem.movie.ids.trakt && oldItem.isHeader() == newItem.isHeader()

  override fun areContentsTheSame(oldItem: ProgressMovieItem, newItem: ProgressMovieItem) =
    oldItem.isPinned == newItem.isPinned &&
      oldItem.image == newItem.image &&
      oldItem.movie == newItem.movie &&
      oldItem.movieTranslation == newItem.movieTranslation
}
