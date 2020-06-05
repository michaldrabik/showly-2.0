package com.michaldrabik.showly2.ui.followedshows.myshows.recycler.section

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.ui.followedshows.myshows.views.section.MyShowsSectionItemView

open class MyShowsSectionAdapter : BaseAdapter<MyShowsItem>() {

  override val asyncDiffer = AsyncListDiffer(this,
    MyShowsSectionDiffCallback()
  )

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      MyShowsSectionItemView(
        parent.context
      )
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as MyShowsSectionItemView).bind(item, missingImageListener, itemClickListener)
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
