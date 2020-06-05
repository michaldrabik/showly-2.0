package com.michaldrabik.showly2.ui.followedshows.seelater.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.followedshows.seelater.views.SeeLaterShowView

class SeeLaterAdapter : BaseAdapter<SeeLaterListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, SeeLaterItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(SeeLaterShowView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as SeeLaterShowView).bind(item, missingImageListener)
  }
}
