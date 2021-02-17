package com.michaldrabik.ui_show.episodes

import android.content.Context
import android.util.AttributeSet
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
import kotlinx.android.synthetic.main.view_episode.view.*
import java.util.Locale.ENGLISH

class EpisodeView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_episode, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    post { episodeCheckbox.expandTouch() }
  }

  fun bind(
    item: EpisodeListItem,
    itemClickListener: (Episode, Boolean) -> Unit,
    itemCheckedListener: (Episode, Boolean) -> Unit,
    isLocked: Boolean
  ) {
    clear()

    val hasAired = item.episode.hasAired(item.season) || item.season.isSpecial()
    episodeTitle.text = String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode.number)
    episodeOverview.text = when {
      !item.translation?.title.isNullOrBlank() -> item.translation?.title
      else -> item.episode.title.ifEmpty { context.getString(R.string.textTba) }
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

  private fun clear() {
    episodeCheckbox.setOnCheckedChangeListener(null)
    episodeMyStarIcon.gone()
    episodeMyRating.gone()
  }
}
