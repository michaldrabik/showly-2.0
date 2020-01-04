package com.michaldrabik.showly2.ui.show.comments

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.showly2.ui.common.views.CommentView

class CommentsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<Comment> = mutableListOf()
  private val asyncDiffer = AsyncListDiffer(this, CommentItemDiffCallback())

  fun setItems(newItems: List<Comment>) = asyncDiffer.submitList(newItems)

  fun clearItems() {
    items.clear()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(CommentView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as CommentView).bind(items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
