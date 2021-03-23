package com.michaldrabik.ui_lists.details.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallbackAdapter
import com.michaldrabik.ui_lists.details.views.ListDetailsMovieItemView
import com.michaldrabik.ui_lists.details.views.ListDetailsShowItemView
import java.util.Collections

//TODO Try drag drop again with asyncdiffer
class ListDetailsAdapter(
  val itemClickListener: (ListDetailsItem) -> Unit,
  val missingImageListener: (ListDetailsItem, Boolean) -> Unit,
  val missingTranslationListener: (ListDetailsItem) -> Unit,
  val itemsChangedListener: () -> Unit,
  val itemsMovedListener: (List<ListDetailsItem>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
  ReorderListCallbackAdapter {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_MOVIE = 2
  }

  private var items = listOf<ListDetailsItem>()

  fun setItems(newItems: List<ListDetailsItem>, notifyItemsChange: Boolean = false) {
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
    VIEW_TYPE_SHOW -> ListDetailsItemViewHolder(ListDetailsShowItemView(parent.context).apply {
      itemClickListener = { item -> this@ListDetailsAdapter.itemClickListener(item) }
      missingImageListener = { item, force -> this@ListDetailsAdapter.missingImageListener(item, force) }
      missingTranslationListener = { item -> this@ListDetailsAdapter.missingTranslationListener(item) }
    })
    VIEW_TYPE_MOVIE -> ListDetailsItemViewHolder(ListDetailsMovieItemView(parent.context).apply {
      itemClickListener = { item -> this@ListDetailsAdapter.itemClickListener(item) }
      missingImageListener = { item, force -> this@ListDetailsAdapter.missingImageListener(item, force) }
      missingTranslationListener = { item -> this@ListDetailsAdapter.missingTranslationListener(item) }
    })
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = items[position]
    when (holder.itemViewType) {
      VIEW_TYPE_SHOW -> (holder.itemView as ListDetailsShowItemView).bind(item, position)
      VIEW_TYPE_MOVIE -> (holder.itemView as ListDetailsMovieItemView).bind(item, position)
      else -> throw IllegalStateException()
    }
  }

  override fun getItemCount() = items.size

  override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    Collections.swap(items, fromPosition, toPosition)
    notifyItemMoved(fromPosition, toPosition)
    return true
  }

  override fun onItemMoveFinished() = itemsMovedListener(items)

  class ListDetailsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
