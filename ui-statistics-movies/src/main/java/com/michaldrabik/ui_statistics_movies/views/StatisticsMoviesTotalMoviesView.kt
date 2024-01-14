package com.michaldrabik.ui_statistics_movies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_statistics_movies.R
import com.michaldrabik.ui_statistics_movies.databinding.ViewStatisticsMoviesCardTotalMoviesBinding
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("SetTextI18n")
class StatisticsMoviesTotalMoviesView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewStatisticsMoviesCardTotalMoviesBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
  }

  fun bind(moviesCount: Int) {
    val formatter = NumberFormat.getNumberInstance(Locale.ENGLISH)
    binding.viewMoviesTotalEpisodesValue.text = context.getString(
      R.string.textStatisticsMoviesTotalMoviesCount,
      formatter.format(moviesCount)
    )
  }
}
