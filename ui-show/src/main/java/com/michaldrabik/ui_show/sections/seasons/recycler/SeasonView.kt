package com.michaldrabik.ui_show.sections.seasons.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.widget.ImageViewCompat
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.ViewSeasonBinding
import java.util.Locale.ENGLISH
import kotlin.math.roundToInt

class SeasonView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewSeasonBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
    post { binding.seasonViewCheckbox.expandTouch() }
  }

  @SuppressLint("SetTextI18n")
  fun bind(
    item: SeasonListItem,
    clickListener: (SeasonListItem) -> Unit,
    itemCheckedListener: (SeasonListItem, Boolean) -> Unit,
  ) {
    clear()
    setOnClickListener { clickListener(item) }
    with(binding) {
      seasonViewTitle.text =
        if (item.season.isSpecial()) context.getString(R.string.textSpecials)
        else String.format(ENGLISH, context.getString(R.string.textSeason), item.season.number)

      val progressCount = item.episodes.count { it.isWatched }
      val episodesCount = item.episodes.size
      var percent = 0
      if (episodesCount != 0) {
        percent = ((progressCount.toFloat() / episodesCount.toFloat()) * 100F).roundToInt()
      }

      seasonViewProgress.max = item.season.episodeCount
      seasonViewProgress.setProgressCompat(item.episodes.count { it.isWatched }, false)
      seasonViewProgressText.text = String.format(ENGLISH, "%d/%d (%d%%)", progressCount, item.episodes.size, percent)

      seasonViewCheckbox.isChecked = item.isWatched
      seasonViewCheckbox.isEnabled = item.episodes.all { it.episode.hasAired(item.season) } || item.isWatched

      val color = context.colorFromAttr(if (item.isWatched) android.R.attr.colorAccent else android.R.attr.textColorPrimary)
      seasonViewTitle.setTextColor(color)
      seasonViewProgressText.setTextColor(color)
      ImageViewCompat.setImageTintList(seasonViewArrow, ColorStateList.valueOf(color))

      seasonViewCheckbox.setOnCheckedChangeListener { _, isChecked ->
        itemCheckedListener(item, isChecked)
      }
    }
  }

  private fun clear() {
    with(binding) {
      seasonViewTitle.text = ""
      seasonViewProgress.progress = 0
      seasonViewProgress.max = 0
      seasonViewCheckbox.setOnCheckedChangeListener(null)
    }
  }
}
