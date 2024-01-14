package com.michaldrabik.ui_comments

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Comment
import java.time.format.DateTimeFormatter

class CommentsAdapter(
  val onDeleteClickListener: ((Comment) -> Unit)? = null,
  val onReplyClickListener: ((Comment) -> Unit)? = null,
  val onRepliesClickListener: ((Comment) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, CommentItemDiffCallback())
  private var dateFormat: DateTimeFormatter? = null

  fun setItems(newItems: List<Comment>, dateFormat: DateTimeFormatter?) {
    this.dateFormat = dateFormat
    asyncDiffer.submitList(newItems)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(
      CommentView(parent.context).apply {
        onRepliesClickListener = this@CommentsAdapter.onRepliesClickListener
        onReplyClickListener = this@CommentsAdapter.onReplyClickListener
        onDeleteClickListener = this@CommentsAdapter.onDeleteClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as CommentView).bind(item, dateFormat)
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
