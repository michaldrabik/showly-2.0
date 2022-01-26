package com.michaldrabik.ui_my_shows.archive.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_my_shows.archive.recycler.views.ArchiveShowView

class ArchiveAdapter(
  val itemClickListener: (ArchiveListItem) -> Unit,
  val itemLongClickListener: (ArchiveListItem) -> Unit,
  val missingImageListener: (ArchiveListItem, Boolean) -> Unit,
  val missingTranslationListener: (ArchiveListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<ArchiveListItem>(
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, ArchiveDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      ArchiveShowView(parent.context).apply {
        itemClickListener = this@ArchiveAdapter.itemClickListener
        itemLongClickListener = this@ArchiveAdapter.itemLongClickListener
        missingImageListener = this@ArchiveAdapter.missingImageListener
        missingTranslationListener = this@ArchiveAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as ArchiveShowView).bind(item)
  }
}
