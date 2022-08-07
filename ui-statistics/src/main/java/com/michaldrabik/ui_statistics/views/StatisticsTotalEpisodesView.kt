package com.michaldrabik.ui_statistics.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_statistics.R
import kotlinx.android.synthetic.main.view_statistics_card_total_episodes.view.*
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("SetTextI18n")
class StatisticsTotalEpisodesView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_card_total_episodes, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
  }

  fun bind(episodesCount: Int, episodesShowsCount: Int) {
    val formatter = NumberFormat.getNumberInstance(Locale.ENGLISH)

    viewTotalEpisodesValue.text = context.getString(
      R.string.textStatisticsTotalEpisodesCount,
      formatter.format(episodesCount)
    )

    viewTotalEpisodesSubValue.text = context.getString(
      R.string.textStatisticsTotalEpisodesShowsCount,
      formatter.format(episodesShowsCount)
    )
  }
}
