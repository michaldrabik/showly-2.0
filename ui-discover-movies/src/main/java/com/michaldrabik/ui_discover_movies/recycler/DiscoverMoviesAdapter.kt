package com.michaldrabik.ui_discover_movies.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_discover_movies.views.MovieFanartView
import com.michaldrabik.ui_discover_movies.views.MoviePosterView
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER

class DiscoverMoviesAdapter(
  itemClickListener: (DiscoverMovieListItem) -> Unit,
  missingImageListener: (DiscoverMovieListItem, Boolean) -> Unit,
  listChangeListener: () -> Unit
) : BaseMovieAdapter<DiscoverMovieListItem>(
  itemClickListener = itemClickListener,
  missingImageListener = missingImageListener,
  listChangeListener = listChangeListener
) {

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, DiscoverMovieItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    POSTER.id -> BaseViewHolder(
      MoviePosterView(parent.context).apply {
        itemClickListener = this@DiscoverMoviesAdapter.itemClickListener
        missingImageListener = this@DiscoverMoviesAdapter.missingImageListener
      }
    )
    FANART.id, FANART_WIDE.id -> BaseViewHolder(
      MovieFanartView(parent.context).apply {
        itemClickListener = this@DiscoverMoviesAdapter.itemClickListener
        missingImageListener = this@DiscoverMoviesAdapter.missingImageListener
      }
    )
    else -> throw IllegalStateException("Unknown view type.")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      POSTER.id ->
        (holder.itemView as MoviePosterView).bind(item)
      FANART.id, FANART_WIDE.id ->
        (holder.itemView as MovieFanartView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) = asyncDiffer.currentList[position].image.type.id
}
