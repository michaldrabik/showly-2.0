package com.michaldrabik.showly2.ui.statistics.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.colorFromAttr
import kotlinx.android.synthetic.main.view_statistics_card_total_time.view.*
import java.text.NumberFormat

@SuppressLint("SetTextI18n")
class StatisticsTotalTimeSpentView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_card_total_time, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
  }

  fun bind(timeMinutes: Long) {
    val formatter = NumberFormat.getNumberInstance()

    val hours = timeMinutes / 60
    val days = hours / 24
    val months = days / 30

    viewTotalTimeSpentHoursValue.text = context.getString(R.string.textStatisticsTotalTimeSpentHours, formatter.format(hours))
    viewTotalTimeSpentMinutesValue.text = context.getString(R.string.textStatisticsTotalTimeSpentMinutes, formatter.format(timeMinutes))

    if (months > 0L) {
      viewTotalTimeSpentSubValue.text = context.getString(
        R.string.textStatisticsTotalTimeSpentMonthsDays,
        months,
        days
      )
    } else {
      viewTotalTimeSpentSubValue.text = context.getString(
        R.string.textStatisticsTotalTimeSpentDays,
        days
      )
    }
  }
}
