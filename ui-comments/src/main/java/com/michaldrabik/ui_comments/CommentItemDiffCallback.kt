package com.michaldrabik.ui_comments

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_model.Comment

class CommentItemDiffCallback : DiffUtil.ItemCallback<Comment>() {

  override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: Comment, newItem: Comment) =
    oldItem == newItem
}
