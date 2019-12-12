package com.michaldrabik.showly2.ui.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import com.michaldrabik.showly2.utilities.extensions.toShortDisplayString
import kotlinx.android.synthetic.main.view_comment.view.*

class CommentView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_comment, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  @SuppressLint("SetTextI18n")
  fun bind(comment: Comment) {
    clear()
    commentHeader.text = "User commented on ${comment.createdAt?.toLocalTimeZone()?.toShortDisplayString()}:"
    commentText.text = comment.comment
  }

  private fun clear() {
    commentHeader.text = ""
    commentText.text = ""
  }
}