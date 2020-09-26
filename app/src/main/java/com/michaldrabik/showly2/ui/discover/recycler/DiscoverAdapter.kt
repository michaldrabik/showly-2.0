package com.michaldrabik.showly2.ui.discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.common.views.ShowFanartView
import com.michaldrabik.showly2.ui.common.views.ShowPosterView
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER

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
