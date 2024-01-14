package com.michaldrabik.ui_base

import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.common.MovieListItem

abstract class BaseMovieAdapter<Item : MovieListItem>(
  val listChangeListener: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncListDiffer.ListListener<Item> {

  abstract val asyncDiffer: AsyncListDiffer<Item>

  private var notifyChange = false

  open fun setItems(newItems: List<Item>, notifyChange: Boolean = false) {
    this.notifyChange = notifyChange
    asyncDiffer.removeListListener(this)
    asyncDiffer.addListListener(this)
    asyncDiffer.submitList(newItems)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  fun getItems(): List<Item> = asyncDiffer.currentList

  fun indexOf(item: Item) = asyncDiffer.currentList.indexOfFirst { it isSameAs item }

  override fun onCurrentListChanged(
    previousList: MutableList<Item>,
    currentList: MutableList<Item>
  ) {
    if (notifyChange) {
      listChangeListener?.invoke()
    }
  }

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
