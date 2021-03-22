package com.michaldrabik.ui_lists.details.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_lists.details.views.ListDetailsMovieItemView
import com.michaldrabik.ui_lists.details.views.ListDetailsShowItemView

class ListDetailsAdapter(
  val itemClickListener: (ListDetailsItem) -> Unit,
  val missingImageListener: (ListDetailsItem, Boolean) -> Unit,
  val missingTranslationListener: (ListDetailsItem) -> Unit,
  val itemsChangedListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncListDiffer.ListListener<ListDetailsItem> {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_MOVIE = 2
  }

  private val asyncDiffer = AsyncListDiffer(this, ListDetailsDiffCallback())
  private var notifyItemsChange = false

  init {
    asyncDiffer.addListListener(this)
  }

  fun setItems(newItems: List<ListDetailsItem>, notifyItemsChange: Boolean = false) {
    this.notifyItemsChange = notifyItemsChange
    asyncDiffer.submitList(newItems)
  }

  override fun getItemViewType(position: Int): Int {
    val item = asyncDiffer.currentList[position]
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
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_SHOW -> (holder.itemView as ListDetailsShowItemView).bind(item)
      VIEW_TYPE_MOVIE -> (holder.itemView as ListDetailsMovieItemView).bind(item)
      else -> throw IllegalStateException()
    }
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  override fun onCurrentListChanged(oldList: MutableList<ListDetailsItem>, newList: MutableList<ListDetailsItem>) {
    if (notifyItemsChange) itemsChangedListener.invoke()
  }

  class ListDetailsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
