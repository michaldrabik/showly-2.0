package com.michaldrabik.ui_progress.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import kotlinx.android.synthetic.main.view_progress_header.view.*

@SuppressLint("SetTextI18n")
class ProgressHeaderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var headerClickListener: ((ProgressListItem.Header) -> Unit)? = null

  private lateinit var item: ProgressListItem.Header

  init {
    inflate(context, R.layout.view_progress_header, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    onClick { headerClickListener?.invoke(item) }
  }

  fun bind(item: ProgressListItem.Header) {
    this.item = item
    progressHeaderText.setText(item.textResId)
    progressHeaderArrow.rotation = if (item.isCollapsed) 0F else 90F
  }
}
