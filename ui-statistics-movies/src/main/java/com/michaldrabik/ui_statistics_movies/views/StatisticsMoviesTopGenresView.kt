package com.michaldrabik.ui_statistics_movies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_statistics_movies.R
import kotlinx.android.synthetic.main.view_statistics_movies_card_top_genre.view.*

@SuppressLint("SetTextI18n")
class StatisticsMoviesTopGenresView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var topGenres = emptyList<Genre>()

  init {
    inflate(context, R.layout.view_statistics_movies_card_top_genre, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setCardBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
    cardElevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    strokeWidth = 0
    onClick {
      showGenres(10)
      isClickable = false
      viewMoviesTopGenresSubValue.text = context.getString(R.string.textStatisticsMoviesTopGenreSubValue2)
    }
  }

  fun bind(genres: List<Genre>) {
    topGenres = genres.toList()
    showGenres(3)
  }

  private fun showGenres(limit: Int) {
    viewMoviesTopGenresValue.text = topGenres
      .take(limit)
      .joinToString("\n") {
        context.getString(it.displayName)
      }
  }
}
