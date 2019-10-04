package com.michaldrabik.showly2.ui.myshows.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.myshows.MyShowListItem
import com.michaldrabik.showly2.ui.myshows.views.MyShowHorizontalView

class MyShowsHorizontalAdapter : BaseAdapter<MyShowListItem>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(MyShowHorizontalView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as MyShowHorizontalView).bind(items[position], missingImageListener, itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}