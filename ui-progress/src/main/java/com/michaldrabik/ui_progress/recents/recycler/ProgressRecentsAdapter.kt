package com.michaldrabik.ui_progress.recents.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_progress.recents.views.ProgressRecentsHeaderView
import com.michaldrabik.ui_progress.recents.views.ProgressRecentsItemView

class ProgressRecentsAdapter : BaseAdapter<RecentsListItem>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, RecentsListItemDiffCallback())

  var detailsClickListener: ((RecentsListItem.Episode) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_ITEM -> BaseViewHolder(
        ProgressRecentsItemView(parent.context).apply {
          itemClickListener = { super.itemClickListener.invoke(it) }
          missingImageListener = { item, force -> super.missingImageListener.invoke(item, force) }
          missingTranslationListener = { super.missingTranslationListener.invoke(it) }
          detailsClickListener = { this@ProgressRecentsAdapter.detailsClickListener?.invoke(it) }
        }
      )
      VIEW_TYPE_HEADER -> BaseViewHolder(ProgressRecentsHeaderView(parent.context))
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is RecentsListItem.Episode -> (holder.itemView as ProgressRecentsItemView).bind(item)
      is RecentsListItem.Header -> (holder.itemView as ProgressRecentsHeaderView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is RecentsListItem.Header -> VIEW_TYPE_HEADER
      is RecentsListItem.Episode -> VIEW_TYPE_ITEM
      else -> throw IllegalStateException()
    }
}
