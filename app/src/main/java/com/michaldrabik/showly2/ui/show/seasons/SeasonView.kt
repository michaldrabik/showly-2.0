package com.michaldrabik.showly2.ui.show.seasons

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import kotlinx.android.synthetic.main.view_season.view.*

class SeasonView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_season, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(item: SeasonListItem, clickListener: (SeasonListItem) -> Unit) {
    clear()
    setOnClickListener { clickListener(item) }

    seasonViewTitle.text = context.getString(R.string.textSeason, item.season.number)
    seasonViewProgress.max = item.season.episodeCount
    seasonViewProgress.progress = item.episodes.count { it.isWatched }
    seasonViewCheckbox.isChecked = item.isWatched
  }

  private fun clear() {
    seasonViewTitle.text = ""
    seasonViewProgress.progress = 0
    seasonViewProgress.max = 0
  }
}