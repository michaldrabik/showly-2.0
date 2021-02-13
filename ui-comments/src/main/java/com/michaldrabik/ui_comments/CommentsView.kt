package com.michaldrabik.ui_comments

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Comment
import kotlinx.android.synthetic.main.view_comments.view.*
import org.threeten.bp.format.DateTimeFormatter

class CommentsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val commentsAdapter by lazy { CommentsAdapter() }
  private var dateFormat: DateTimeFormatter? = null

  var onRepliesClickListener: ((Comment) -> Unit)? = null
  var onPostCommentClickListener: ((Comment?) -> Unit)? = null
  var onDeleteCommentClickListener: ((Comment) -> Unit)? = null

  init {
    inflate(context, R.layout.view_comments, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setupRecycler()
    commentsPostButton.onClick { onPostCommentClickListener?.invoke(null) }
  }

  fun bind(
    comments: List<Comment>,
    dateFormat: DateTimeFormatter?
  ) {
    this.dateFormat = dateFormat
    commentsProgress.gone()
    commentsEmpty.visibleIf(comments.isEmpty())
    commentsAdapter.setItems(comments, dateFormat)
  }

  fun clear() {
    commentsProgress.visible()
    commentsEmpty.gone()
    commentsAdapter.setItems(emptyList(), dateFormat)
    hideCommentButton()
  }

  private fun setupRecycler() {
    commentsRecycler.apply {
      setHasFixedSize(true)
      adapter = commentsAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
      addDivider(R.drawable.divider_comments_list)
    }
    commentsAdapter.run {
      onRepliesClickListener = { this@CommentsView.onRepliesClickListener?.invoke(it) }
      onDeleteClickListener = { this@CommentsView.onDeleteCommentClickListener?.invoke(it) }
    }
  }

  fun showCommentButton() {
    commentsPostButton.fadeIn(duration = 200, startDelay = 100)
  }

  fun hideCommentButton() {
    commentsPostButton.gone()
  }
}
