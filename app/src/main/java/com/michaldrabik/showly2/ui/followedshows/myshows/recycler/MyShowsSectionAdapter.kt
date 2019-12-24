package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowHorizontalView

class MyShowsSectionAdapter : BaseAdapter<MyShowsListItem>() {

  override val asyncDiffer = AsyncListDiffer(this, MyShowsListItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(MyShowHorizontalView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as MyShowHorizontalView).bind(item, missingImageListener, itemClickListener)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
