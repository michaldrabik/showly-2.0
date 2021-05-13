package com.michaldrabik.ui_my_movies.mymovies.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieHeaderView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMoviesRecentsView

class MyMoviesAdapter(
  itemClickListener: (MyMoviesItem) -> Unit,
  missingImageListener: (MyMoviesItem, Boolean) -> Unit,
  missingTranslationListener: (MyMoviesItem) -> Unit,
  val onSortOrderClickListener: (MyMoviesSection, SortOrder) -> Unit
) : BaseMovieAdapter<MyMoviesItem>(
  itemClickListener = itemClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener
) {

  companion object {
    private const val VIEW_TYPE_HEADER = 1
    private const val VIEW_TYPE_MOVIE_ITEM = 2
    private const val VIEW_TYPE_RECENTS_SECTION = 3
  }

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, MyMoviesItemDiffCallback())
  var notifyListsUpdate = false

  override fun setItems(newItems: List<MyMoviesItem>, notifyChange: Boolean) {
    super.setItems(newItems, notifyListsUpdate)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_HEADER -> BaseViewHolder(MyMovieHeaderView(parent.context))
      VIEW_TYPE_RECENTS_SECTION -> BaseViewHolder(MyMoviesRecentsView(parent.context))
      VIEW_TYPE_MOVIE_ITEM -> BaseViewHolder(
        MyMovieAllView(parent.context).apply {
          itemClickListener = this@MyMoviesAdapter.itemClickListener
          missingImageListener = this@MyMoviesAdapter.missingImageListener
          missingTranslationListener = this@MyMoviesAdapter.missingTranslationListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as MyMovieHeaderView).bind(
        item.header!!,
        onSortOrderClickListener
      )
      VIEW_TYPE_RECENTS_SECTION -> (holder.itemView as MyMoviesRecentsView).bind(
        item.recentsSection!!,
        itemClickListener
      )
      VIEW_TYPE_MOVIE_ITEM -> (holder.itemView as MyMovieAllView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position].type) {
      MyMoviesItem.Type.HEADER -> VIEW_TYPE_HEADER
      MyMoviesItem.Type.ALL_MOVIES_ITEM -> VIEW_TYPE_MOVIE_ITEM
      MyMoviesItem.Type.RECENT_MOVIE -> VIEW_TYPE_RECENTS_SECTION
      else -> throw IllegalStateException()
    }
}
