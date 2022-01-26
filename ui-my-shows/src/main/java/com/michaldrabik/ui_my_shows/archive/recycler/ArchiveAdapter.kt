package com.michaldrabik.ui_my_shows.archive.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_my_shows.archive.recycler.views.ArchiveShowView

class ArchiveAdapter(
  itemClickListener: (ArchiveListItem) -> Unit,
  itemLongClickListener: (ArchiveListItem) -> Unit,
  missingImageListener: (ArchiveListItem, Boolean) -> Unit,
  missingTranslationListener: (ArchiveListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<ArchiveListItem>(
  itemClickListener = itemClickListener,
  itemLongClickListener = itemLongClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener,
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
