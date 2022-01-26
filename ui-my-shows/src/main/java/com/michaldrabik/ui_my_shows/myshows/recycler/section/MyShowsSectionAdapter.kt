package com.michaldrabik.ui_my_shows.myshows.recycler.section

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_my_shows.myshows.views.section.MyShowsSectionItemView

open class MyShowsSectionAdapter(
  itemClickListener: (MyShowsItem) -> Unit,
  itemLongClickListener: (MyShowsItem) -> Unit,
  missingImageListener: (MyShowsItem, Boolean) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<MyShowsItem>(
  itemClickListener = itemClickListener,
  itemLongClickListener = itemLongClickListener,
  missingImageListener = missingImageListener,
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, MyShowsSectionDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      MyShowsSectionItemView(parent.context).apply {
        itemClickListener = this@MyShowsSectionAdapter.itemClickListener
        itemLongClickListener = this@MyShowsSectionAdapter.itemLongClickListener
        missingImageListener = this@MyShowsSectionAdapter.missingImageListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as MyShowsSectionItemView).bind(item)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
