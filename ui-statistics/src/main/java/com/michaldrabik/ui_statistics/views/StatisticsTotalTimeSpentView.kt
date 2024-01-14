package com.michaldrabik.ui_statistics.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_statistics.R
import com.michaldrabik.ui_statistics.databinding.ViewStatisticsCardTotalTimeBinding
import java.text.NumberFormat
import java.util.Locale.ENGLISH
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class StatisticsTotalTimeSpentView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewStatisticsCardTotalTimeBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
  }

  fun bind(timeMinutes: Int) {
    val formatter = NumberFormat.getNumberInstance(ENGLISH)

    val hours = TimeUnit.MINUTES.toHours(timeMinutes.toLong())
    val days = TimeUnit.HOURS.toDays(hours)

    with(binding) {
      viewTotalTimeSpentHoursValue.text = context.getString(R.string.textStatisticsTotalTimeSpentHours, formatter.format(hours))
      viewTotalTimeSpentMinutesValue.text = context.getString(R.string.textStatisticsTotalTimeSpentMinutes, formatter.format(timeMinutes))
      viewTotalTimeSpentSubValue.text = context.getString(R.string.textStatisticsTotalTimeSpentDays, formatter.format(days))
    }
  }
}
