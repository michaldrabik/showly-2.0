package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowAllView

class MyShowsAllSectionAdapter : MyShowsSectionAdapter() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(MyShowAllView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as MyShowAllView).bind(item, missingImageListener, itemClickListener)
  }
}
