package com.michaldrabik.showly2.ui.show.seasons

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import kotlinx.android.synthetic.main.view_season.view.*

class SeasonView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_season, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
    post { seasonViewCheckbox.expandTouchArea() }
  }

  @SuppressLint("SetTextI18n")
  fun bind(
    item: SeasonListItem,
    clickListener: (SeasonListItem) -> Unit,
    itemCheckedListener: (SeasonListItem, Boolean) -> Unit
  ) {
    clear()
    setOnClickListener { clickListener(item) }

    seasonViewTitle.text = context.getString(R.string.textSeason, item.season.number)
    seasonViewProgress.max = item.season.episodeCount
    val progressCount = item.episodes.count { it.isWatched }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      seasonViewProgress.setProgress(progressCount, true)
    } else {
      seasonViewProgress.progress = progressCount
    }

    seasonViewProgressText.text = "$progressCount/${item.episodes.size}"
    seasonViewCheckbox.isChecked = item.isWatched

    val color = ContextCompat.getColor(context, if (item.isWatched) R.color.colorAccent else R.color.colorTextPrimary)
    seasonViewTitle.setTextColor(color)
    seasonViewProgressText.setTextColor(color)
    ImageViewCompat.setImageTintList(seasonViewArrow, ColorStateList.valueOf(color))

    seasonViewCheckbox.setOnCheckedChangeListener { _, isChecked ->
      itemCheckedListener(item, isChecked)
    }
  }

  private fun clear() {
    seasonViewTitle.text = ""
    seasonViewProgress.progress = 0
    seasonViewProgress.max = 0
    seasonViewCheckbox.setOnCheckedChangeListener { _, _ -> }
  }
}