package com.michaldrabik.showly2.ui.followedshows.archive.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.followedshows.archive.recycler.views.ArchiveShowView

class ArchiveAdapter : BaseAdapter<ArchiveListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, ArchiveDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(ArchiveShowView(parent.context).apply {
      itemClickListener = { super.itemClickListener.invoke(it) }
    })

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ArchiveShowView).bind(item, missingImageListener)
  }
}
