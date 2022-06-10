package com.michaldrabik.ui_show.episodes.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.ViewEpisodeBinding
import java.util.Locale.ENGLISH

class EpisodeView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewEpisodeBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    post { binding.episodeCheckbox.expandTouch() }
  }

  fun bind(
    item: EpisodeListItem,
    itemClickListener: (Episode, Boolean) -> Unit,
    itemCheckedListener: (Episode, Boolean) -> Unit,
    isLocked: Boolean
  ) {
    clear()
    with(binding) {
      val hasAired = item.episode.hasAired(item.season) || item.season.isSpecial()
      val episodeTitleText = String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode.number)
        .plus(item.episode.numberAbs?.let { if (it > 0 && item.isAnime) " ($it)" else "" } ?: "")

      episodeTitle.text = episodeTitleText
      episodeOverview.text = when {
        !item.translation?.title.isNullOrBlank() -> item.translation?.title
        item.episode.title.isEmpty() -> context.getString(R.string.textTba)
        item.episode.title == "Episode ${item.episode.number}" -> episodeTitleText
        else -> item.episode.title
      }
      episodeCheckbox.isChecked = item.isWatched
      episodeCheckbox.isEnabled = hasAired || !isLocked

      episodeRating.visibleIf(item.episode.rating != 0F)
      episodeRating.text = String.format(ENGLISH, "%.1f", item.episode.rating)

      item.myRating?.let {
        episodeMyStarIcon.visible()
        episodeMyRating.visible()
        episodeMyRating.text = String.format(ENGLISH, "%d", item.myRating.rating)
      }

      if (!hasAired) {
        val date = item.episode.firstAired?.toLocalZone()
        val displayDate = date?.let { item.dateFormat?.format(it)?.capitalizeWords() } ?: context.getString(R.string.textTba)
        episodeTitle.text = String.format(ENGLISH, context.getString(R.string.textEpisodeDate), item.episode.number, displayDate)
      }

      episodeCheckbox.setOnCheckedChangeListener { _, isChecked -> itemCheckedListener(item.episode, isChecked) }
      onClick { itemClickListener(item.episode, item.isWatched) }
    }
  }

  private fun clear() {
    with(binding) {
      episodeCheckbox.setOnCheckedChangeListener(null)
      episodeMyStarIcon.gone()
      episodeMyRating.gone()
    }
  }
}
