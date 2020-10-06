package com.michaldrabik.ui_discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_discover.views.ShowFanartView
import com.michaldrabik.ui_discover.views.ShowPosterView
import com.michaldrabik.ui_model.ImageType.*

class DiscoverAdapter : BaseAdapter<DiscoverListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, DiscoverItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    POSTER.id -> BaseViewHolder(ShowPosterView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })
    FANART.id, FANART_WIDE.id -> BaseViewHolder(ShowFanartView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })
    else -> throw IllegalStateException("Unknown view type.")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      POSTER.id ->
        (holder.itemView as ShowPosterView).bind(item, missingImageListener)
      FANART.id, FANART_WIDE.id ->
        (holder.itemView as ShowFanartView).bind(item, missingImageListener)
    }
  }

  override fun getItemViewType(position: Int) = asyncDiffer.currentList[position].image.type.id
}
