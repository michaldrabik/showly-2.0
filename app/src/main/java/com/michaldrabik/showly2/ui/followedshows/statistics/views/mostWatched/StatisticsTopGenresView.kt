package com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.showly2.R
import kotlinx.android.synthetic.main.view_statistics_card_top_genre.view.*

@SuppressLint("SetTextI18n")
class StatisticsTopGenresView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_card_top_genre, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorStatisticsCardBackground))
  }

  fun bind(genres: List<String>) {
    viewTopGenresValue.text = genres.joinToString("\n") { it[0].toUpperCase() + it.substring(1) }
  }
}
