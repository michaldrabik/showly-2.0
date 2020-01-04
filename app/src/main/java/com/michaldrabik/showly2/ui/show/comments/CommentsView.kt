package com.michaldrabik.showly2.ui.show.comments

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R

class CommentsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val commentsAdapter by lazy { CommentsAdapter() }

  init {
    inflate(context, R.layout.view_comments, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

}
