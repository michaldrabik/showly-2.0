package com.michaldrabik.ui_movie.sections.collections.list.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_model.MovieCollection

class MovieCollectionDiffCallback : DiffUtil.ItemCallback<MovieCollection>() {

  override fun areItemsTheSame(oldItem: MovieCollection, newItem: MovieCollection) =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: MovieCollection, newItem: MovieCollection) =
    oldItem == newItem
}
