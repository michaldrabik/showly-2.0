package com.michaldrabik.ui_statistics_movies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_statistics_movies.R
import kotlinx.android.synthetic.main.view_statistics_movies_card_total_time.view.*
import java.text.NumberFormat
import java.util.Locale.ENGLISH
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class StatisticsMoviesTotalTimeSpentView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_movies_card_total_time, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
  }

  fun bind(timeMinutes: Int) {
    val formatter = NumberFormat.getNumberInstance(ENGLISH)

    val hours = TimeUnit.MINUTES.toHours(timeMinutes.toLong())
    val days = TimeUnit.HOURS.toDays(hours)

    viewMoviesTotalTimeSpentHoursValue.text =
      context.getString(R.string.textStatisticsMoviesTotalTimeSpentHours, formatter.format(hours))

    viewMoviesTotalTimeSpentMinutesValue.text =
      context.getString(R.string.textStatisticsMoviesTotalTimeSpentMinutes, formatter.format(timeMinutes))

    viewMoviesTotalTimeSpentSubValue.text =
      context.getString(R.string.textStatisticsMoviesTotalTimeSpentDays, formatter.format(days))
  }
}
