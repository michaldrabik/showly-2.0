package com.michaldrabik.ui_progress.recents.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.recents.recycler.RecentsListItem
import kotlinx.android.synthetic.main.view_progress_recents_header.view.*

class ProgressRecentsHeaderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_progress_recents_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    updatePadding(
      top = context.dimenToPx(R.dimen.spaceNormal),
      bottom = context.dimenToPx(R.dimen.spaceTiny)
    )
  }

  fun bind(item: RecentsListItem.Header) {
    progressRecentsHeaderText.setText(item.textResId)
  }
}
