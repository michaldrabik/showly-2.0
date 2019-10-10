package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_episode.view.*

class EpisodeView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_episode, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    post { episodeCheckbox.expandTouchArea() }
  }

  fun bind(
    item: EpisodeListItem,
    itemClickListener: (Episode, Boolean) -> Unit,
    itemCheckedListener: (Episode, Boolean) -> Unit
  ) {
    clear()

    episodeTitle.text = context.getString(R.string.textEpisode, item.episode.number)
    episodeOverview.text = item.episode.title
    episodeCheckbox.isChecked = item.isWatched

    if (item.episode.hasAired()) {
      episodeCheckbox.setOnCheckedChangeListener { _, isChecked ->
        itemCheckedListener(item.episode, isChecked)
      }
    } else {
      val date = item.episode.firstAired?.toLocalTimeZone()
      episodeTitle.text = context.getString(R.string.textEpisodeDate, item.episode.number, date?.toDisplayString() ?: "TBA")
      episodeCheckbox.isEnabled = false
    }

    onClick { itemClickListener(item.episode, item.isWatched) }
  }

  private fun clear() {
    episodeCheckbox.setOnCheckedChangeListener { _, _ -> }
    episodeCheckbox.isEnabled = true
  }
}