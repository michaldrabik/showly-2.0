package com.michaldrabik.showly2.ui.followedshows.statistics.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.colorFromAttr
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_statistics_card_top_genre.view.*

@SuppressLint("SetTextI18n")
class StatisticsTopGenresView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var topGenres = emptyList<String>()

  init {
    inflate(context, R.layout.view_statistics_card_top_genre, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    onClick {
      showGenres(10)
      isClickable = false
      viewTopGenresSubValue.text = context.getString(R.string.textStatisticsTopGenreSubValue2)
    }
  }

  fun bind(genres: List<String>) {
    topGenres = genres.toList()
    showGenres(3)
  }

  private fun showGenres(limit: Int) {
    viewTopGenresValue.text = topGenres
      .take(limit)
      .joinToString("\n") {
        it[0].toUpperCase() + it.substring(1)
      }
  }
}
