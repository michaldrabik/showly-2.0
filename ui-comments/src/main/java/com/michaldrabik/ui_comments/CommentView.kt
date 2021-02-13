package com.michaldrabik.ui_comments

import android.annotation.SuppressLint
import android.content.Context
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
import com.michaldrabik.ui_model.Comment
import kotlinx.android.synthetic.main.view_comment.view.*
import org.threeten.bp.format.DateTimeFormatter
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
    commentDelete.onClick { onDeleteClickListener?.invoke(comment) }
  }

  var onRepliesClickListener: ((Comment) -> Unit)? = null
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
    commentRepliesCount.text = comment.replies.toString()
    commentReplies.visibleIf(comment.replies > 0 && !comment.isLoading)
    commentRepliesCount.visibleIf(comment.replies > 0 && !comment.isLoading)
    commentRepliesCount.text = comment.replies.toString()
    commentProgress.visibleIf(comment.isLoading || comment.isDeleting)
    commentSpacerLine.visibleIf(comment.isReply())
    commentDelete.visibleIf(comment.isMe && comment.replies == 0L)

    if (comment.hasSpoilers()) {
      with(commentText) {
        text = context.getString(R.string.textSpoilersWarning)
        setTextColor(colorTextAccent)
        onClick {
          text = comment.comment
          setTextColor(colorTextPrimary)
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
    commentHeader.text = ""
    commentDate.text = ""
    commentText.text = ""
    commentRepliesCount.text = ""
    commentText.setTextColor(colorTextPrimary)
    commentDate.setTextColor(colorTextSecondary)
    commentHeader.setTextColor(colorTextSecondary)
    Glide.with(this).clear(commentImage)
  }
}
