package com.michaldrabik.ui_movie.sections.collections.details.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.HeaderItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.LoadingItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.MovieItem

class MovieDetailsCollectionItemDiffCallback : DiffUtil.ItemCallback<MovieDetailsCollectionItem>() {

  override fun areItemsTheSame(oldItem: MovieDetailsCollectionItem, newItem: MovieDetailsCollectionItem) =
    when {
      oldItem is HeaderItem && newItem is HeaderItem -> true
      oldItem is MovieItem && newItem is MovieItem && oldItem.id == newItem.id -> true
      oldItem is LoadingItem && newItem is LoadingItem -> true
      else -> false
    }

  override fun areContentsTheSame(oldItem: MovieDetailsCollectionItem, newItem: MovieDetailsCollectionItem) =
    when {
      oldItem is HeaderItem && newItem is HeaderItem -> oldItem == newItem
      oldItem is MovieItem && newItem is MovieItem -> oldItem == newItem
      else -> false
    }
}
