package com.michaldrabik.showly2.ui.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Comment
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.toDayOnlyDisplayString
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import kotlinx.android.synthetic.main.view_comment.view.*

class CommentView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_comment, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  private val colorTextNormal by lazy { ContextCompat.getColor(context, R.color.colorTextPrimary) }
  private val colorTextSpoiler by lazy { ContextCompat.getColor(context, R.color.colorAccent) }

  @SuppressLint("SetTextI18n", "DefaultLocale")
  fun bind(comment: Comment) {
    clear()
    commentHeader.text = "${comment.user.username.capitalize()} commented on ${comment.createdAt?.toLocalTimeZone()?.toDayOnlyDisplayString()}:"

    if (comment.hasSpoilers()) {
      commentText.text = context.getString(R.string.textSpoilersWarning)
      commentText.setTextColor(colorTextSpoiler)
      onClick {
        commentText.text = comment.comment
        commentText.setTextColor(colorTextNormal)
      }
    } else {
      commentText.text = comment.comment
    }

    if (comment.user.avatarUrl.isNotEmpty()) {
      Glide.with(this)
        .load(comment.user.avatarUrl)
        .transform(CircleCrop())
        .into(commentImage)
    }
  }

  private fun clear() {
    onClick { /* NOOP */ }
    commentHeader.text = ""
    commentText.text = ""
    commentText.setTextColor(colorTextNormal)
    Glide.with(this).clear(commentImage)
  }
}
