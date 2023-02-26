package com.michaldrabik.ui_lists.details.recycler

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_lists.details.helpers.ListItemDragListener
import com.michaldrabik.ui_lists.details.helpers.ListItemSwipeListener
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallbackAdapter
import com.michaldrabik.ui_lists.details.views.ListDetailsItemView
import com.michaldrabik.ui_lists.details.views.ListDetailsMovieItemView
import com.michaldrabik.ui_lists.details.views.ListDetailsShowItemView
import com.michaldrabik.ui_lists.details.views.compact.ListDetailsCompactMovieItemView
import com.michaldrabik.ui_lists.details.views.compact.ListDetailsCompactShowItemView
import com.michaldrabik.ui_lists.details.views.grid.ListDetailsGridItemView
import com.michaldrabik.ui_lists.details.views.grid.ListDetailsGridTitleItemView
import java.util.Collections

class ListDetailsAdapter(
  val itemClickListener: (ListDetailsItem) -> Unit,
  val missingImageListener: (ListDetailsItem, Boolean) -> Unit,
  val missingTranslationListener: (ListDetailsItem) -> Unit,
  val itemsChangedListener: () -> Unit,
  val itemsClearedListener: (List<ListDetailsItem>) -> Unit,
  val itemsSwipedListener: (ListDetailsItem) -> Unit,
  val itemDragStartListener: ListItemDragListener,
  val itemSwipeStartListener: ListItemSwipeListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
  ReorderListCallbackAdapter {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_MOVIE = 2
  }

  var items = listOf<ListDetailsItem>()

  var listViewMode: ListViewMode = LIST_NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, items.size)
    }

  fun setItems(newItems: List<ListDetailsItem>, notifyItemsChange: Boolean) {
    // Using old DiffUtil method here because of drag and drop issues with asyncDiff.
    val diff = DiffUtil.calculateDiff(ListDetailsDiffCallback(items, newItems))
    diff.dispatchUpdatesTo(this)
    items = newItems
    if (notifyItemsChange) itemsChangedListener.invoke()
  }

  override fun getItemViewType(position: Int): Int {
    val item = items[position]
    return when {
      item.isShow() -> VIEW_TYPE_SHOW
      item.isMovie() -> VIEW_TYPE_MOVIE
      else -> throw IllegalStateException()
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    VIEW_TYPE_SHOW -> {
      val view = when (listViewMode) {
        LIST_NORMAL -> ListDetailsShowItemView(parent.context)
        LIST_COMPACT -> ListDetailsCompactShowItemView(parent.context)
        GRID -> ListDetailsGridItemView(parent.context)
        GRID_TITLE -> ListDetailsGridTitleItemView(parent.context)
      }.apply {
        itemClickListener = { item -> this@ListDetailsAdapter.itemClickListener(item) }
        missingImageListener = { item, force -> this@ListDetailsAdapter.missingImageListener(item, force) }
        missingTranslationListener = { item -> this@ListDetailsAdapter.missingTranslationListener(item) }
      }
      ListDetailsItemViewHolder(
        view,
        itemDragStartListener,
        itemSwipeStartListener
      )
    }
    VIEW_TYPE_MOVIE -> {
      val view = when (listViewMode) {
        LIST_NORMAL -> ListDetailsMovieItemView(parent.context)
        LIST_COMPACT -> ListDetailsCompactMovieItemView(parent.context)
        GRID -> ListDetailsGridItemView(parent.context)
        GRID_TITLE -> ListDetailsGridTitleItemView(parent.context)
      }.apply {
        itemClickListener = { item -> this@ListDetailsAdapter.itemClickListener(item) }
        missingImageListener = { item, force -> this@ListDetailsAdapter.missingImageListener(item, force) }
        missingTranslationListener = { item -> this@ListDetailsAdapter.missingTranslationListener(item) }
      }
      ListDetailsItemViewHolder(
        view,
        itemDragStartListener,
        itemSwipeStartListener
      )
    }
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = items[position]
    when (holder.itemViewType) {
      VIEW_TYPE_SHOW -> when (listViewMode) {
        LIST_NORMAL -> (holder.itemView as ListDetailsShowItemView).bind(item)
        LIST_COMPACT -> (holder.itemView as ListDetailsCompactShowItemView).bind(item)
        GRID -> (holder.itemView as ListDetailsGridItemView).bind(item)
        GRID_TITLE -> (holder.itemView as ListDetailsGridTitleItemView).bind(item)
      }
      VIEW_TYPE_MOVIE -> when (listViewMode) {
        LIST_NORMAL -> (holder.itemView as ListDetailsMovieItemView).bind(item)
        LIST_COMPACT -> (holder.itemView as ListDetailsCompactMovieItemView).bind(item)
        GRID -> (holder.itemView as ListDetailsGridItemView).bind(item)
        GRID_TITLE -> (holder.itemView as ListDetailsGridTitleItemView).bind(item)
      }
      else -> throw IllegalStateException()
    }
  }

  override fun getItemCount() = items.size

  override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(items, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(items, i, i - 1)
      }
    }
    notifyItemMoved(fromPosition, toPosition)
    return true
  }

  override fun onItemCleared() = itemsClearedListener(items)

  override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder) {
    val item = ((viewHolder as ListDetailsItemViewHolder).itemView as ListDetailsItemView).item
    itemsSwipedListener(item)
  }

  @SuppressLint("ClickableViewAccessibility")
  class ListDetailsItemViewHolder(
    itemView: ListDetailsItemView,
    dragStartListener: ListItemDragListener,
    swipeStartListener: ListItemSwipeListener
  ) : RecyclerView.ViewHolder(itemView) {
    init {
      itemView.itemDragStartListener = {
        dragStartListener.onListItemDragStarted(this)
      }
      itemView.itemSwipeStartListener = {
        swipeStartListener.onListItemSwipeStarted(this)
      }
    }
  }
}
