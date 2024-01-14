package com.michaldrabik.ui_show.episodes.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
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
  }

  fun bind(
    item: EpisodeListItem,
    itemClickListener: (Episode, Boolean) -> Unit,
    itemCheckedListener: (Episode, Boolean) -> Unit,
    isLocked: Boolean
  ) {
    clear()
    with(binding) {
      bindTitle(item)

      val hasAired = item.episode.hasAired(item.season) || item.season.isSpecial()
      episodeCheckbox.isChecked = item.isWatched
      episodeCheckbox.isEnabled = hasAired || !isLocked

      val rating = String.format(ENGLISH, "%.1f", item.episode.rating)
      episodeRating.visibleIf(item.episode.rating != 0F)
      if (!item.isWatched && item.spoilers.isEpisodeRatingHidden) {
        episodeRating.tag = rating
        episodeRating.text = Config.SPOILERS_RATINGS_HIDE_SYMBOL
        if (item.spoilers.isTapToReveal) {
          with(episodeRating) {
            onClick {
              tag?.let { text = it.toString() }
              isClickable = false
            }
          }
        }
      } else {
        episodeRating.text = rating
      }

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

  private fun bindTitle(item: EpisodeListItem) {
    with(binding) {
      val titleText = String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode.number)
        .plus(item.episode.numberAbs?.let { if (it > 0 && item.isAnime) " ($it)" else "" } ?: "")

      var overviewText = when {
        !item.translation?.title.isNullOrBlank() -> item.translation?.title
        item.episode.title.isEmpty() -> context.getString(R.string.textTba)
        item.episode.title == "Episode ${item.episode.number}" -> titleText
        else -> item.episode.title
      }

      if (!item.isWatched && item.spoilers.isEpisodeTitleHidden) {
        episodeOverview.tag = overviewText.toString()
        overviewText = SPOILERS_REGEX.replace(overviewText.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isTapToReveal) {
          episodeOverview.onClick { view ->
            view.tag?.let { episodeOverview.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      episodeTitle.text = titleText
      episodeOverview.text = overviewText
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
