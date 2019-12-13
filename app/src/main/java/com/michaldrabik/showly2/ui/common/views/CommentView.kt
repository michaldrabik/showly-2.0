package com.michaldrabik.showly2.ui.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.showly2.R
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

  @SuppressLint("SetTextI18n", "DefaultLocale")
  fun bind(comment: Comment) {
    clear()
    commentHeader.text = "${comment.user.username.capitalize()} commented on ${comment.createdAt?.toLocalTimeZone()?.toDayOnlyDisplayString()}:"
    commentText.text = comment.comment

    if (comment.user.avatarUrl.isNotEmpty()) {
      Glide.with(this)
        .load(comment.user.avatarUrl)
        .transform(CircleCrop())
        .into(commentImage)
    }
  }

  private fun clear() {
    commentHeader.text = ""
    commentText.text = ""
    Glide.with(this).clear(commentImage)
  }
}