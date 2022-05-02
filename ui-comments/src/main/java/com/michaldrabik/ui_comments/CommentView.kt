package com.michaldrabik.ui_comments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_comments.utilities.refreshTextSelection
import com.michaldrabik.ui_model.Comment
import kotlinx.android.synthetic.main.view_comment.view.*
import java.time.format.DateTimeFormatter
import java.util.Locale

class CommentView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val colorTextPrimary by lazy { context.colorFromAttr(android.R.attr.textColorPrimary) }
  private val colorTextSecondary by lazy { context.colorFromAttr(android.R.attr.textColorSecondary) }
  private val colorTextAccent by lazy { context.colorFromAttr(android.R.attr.colorAccent) }
  private val commentSpace by lazy { context.dimenToPx(R.dimen.commentViewSpace) }

  init {
    inflate(context, R.layout.view_comment, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    arrayOf(commentReplies, commentRepliesCount).forEach {
      with(it) {
        expandTouch()
        onClick {
          if (!comment.isLoading) {
            onRepliesClickListener?.invoke(comment)
          }
        }
      }
    }
    commentReply.onClick { onReplyClickListener?.invoke(comment) }
    commentDelete.onClick { onDeleteClickListener?.invoke(comment) }
  }

  var onRepliesClickListener: ((Comment) -> Unit)? = null
  var onReplyClickListener: ((Comment) -> Unit)? = null
  var onDeleteClickListener: ((Comment) -> Unit)? = null

  private lateinit var comment: Comment

  @SuppressLint("SetTextI18n", "DefaultLocale")
  fun bind(comment: Comment, dateFormat: DateTimeFormatter?) {
    clear()
    this.comment = comment

    commentSpacer.setGuidelineBegin(if (comment.isReply()) commentSpace else 0)
    commentHeader.text = context.getString(R.string.textCommentedOn, comment.user.username)
    commentDate.text = comment.updatedAt?.toLocalZone()?.let { dateFormat?.format(it) }

    if (comment.isMe) {
      commentDate.setTextColor(colorTextAccent)
      commentHeader.setTextColor(colorTextAccent)
    }

    commentRating.visibleIf(comment.userRating > 0)
    commentRating.text = String.format(Locale.ENGLISH, "%d", comment.userRating)
    commentReplies.visibleIf(comment.replies > 0 && !comment.isLoading && !comment.hasRepliesLoaded)
    commentRepliesCount.visibleIf(comment.replies > 0 && !comment.isLoading && !comment.hasRepliesLoaded)
    commentRepliesCount.text = comment.replies.toString()
    commentProgress.visibleIf(comment.isLoading || comment.isLoading)
    commentSpacerLine.visibleIf(comment.isReply())
    commentReply.visibleIf(comment.isSignedIn && !comment.isLoading)
    commentDelete.visibleIf(comment.isSignedIn && comment.isMe && comment.replies == 0L && !comment.isLoading)

    if (comment.hasSpoilers()) {
      with(commentText) {
        text = context.getString(R.string.textSpoilersWarning)
        commentText.setTypeface(null, Typeface.BOLD_ITALIC)
        commentText.setTextColor(colorTextSecondary)
        onClick {
          text = comment.comment
          commentText.setTypeface(null, Typeface.NORMAL)
          commentText.setTextColor(colorTextPrimary)
        }
      }
    } else {
      commentText.text = comment.comment
    }

    if (comment.user.avatarUrl.isNotEmpty()) {
      Glide.with(this)
        .load(comment.user.avatarUrl)
        .placeholder(R.drawable.ic_person_placeholder)
        .transform(CircleCrop())
        .into(commentImage)
    }
  }

  private fun clear() {
    commentText.refreshTextSelection()
    commentText.setTypeface(null, Typeface.NORMAL)
    commentText.setTextColor(colorTextPrimary)
    commentDate.setTextColor(colorTextSecondary)
    commentHeader.setTextColor(colorTextSecondary)
    Glide.with(this).clear(commentImage)
  }
}
