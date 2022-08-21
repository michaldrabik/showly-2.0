package com.michaldrabik.ui_my_movies.hidden.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.filters.FollowedMoviesFiltersView
import com.michaldrabik.ui_my_movies.hidden.recycler.views.HiddenMovieView

class HiddenAdapter(
  private val itemClickListener: (HiddenListItem) -> Unit,
  private val itemLongClickListener: (HiddenListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val missingImageListener: (HiddenListItem, Boolean) -> Unit,
  private val missingTranslationListener: (HiddenListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<HiddenListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_MOVIE = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, HiddenDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_MOVIE -> BaseViewHolder(
        HiddenMovieView(parent.context).apply {
          itemClickListener = this@HiddenAdapter.itemClickListener
          itemLongClickListener = this@HiddenAdapter.itemLongClickListener
          missingImageListener = this@HiddenAdapter.missingImageListener
          missingTranslationListener = this@HiddenAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseViewHolder(
        FollowedMoviesFiltersView(parent.context).apply {
          onSortChipClicked = this@HiddenAdapter.sortChipClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is HiddenListItem.FiltersItem ->
        (holder.itemView as FollowedMoviesFiltersView).bind(item.sortOrder, item.sortType)
      is HiddenListItem.MovieItem ->
        (holder.itemView as HiddenMovieView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is HiddenListItem.MovieItem -> VIEW_TYPE_MOVIE
      is HiddenListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
