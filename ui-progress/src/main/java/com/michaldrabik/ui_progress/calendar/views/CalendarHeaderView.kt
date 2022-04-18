package com.michaldrabik.ui_progress.calendar.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import kotlinx.android.synthetic.main.view_calendar_header.view.*

class CalendarHeaderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_calendar_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    updatePadding(
      top = context.dimenToPx(R.dimen.spaceNormal),
      bottom = context.dimenToPx(R.dimen.spaceTiny),
      left = context.dimenToPx(R.dimen.spaceMedium),
      right = context.dimenToPx(R.dimen.spaceMedium)
    )
  }

  fun bind(item: CalendarListItem.Header) {
    calendarHeaderText.setText(item.textResId)
    calendarHeaderIcon.visibleIf(item.calendarMode == CalendarMode.RECENTS)
  }
}
