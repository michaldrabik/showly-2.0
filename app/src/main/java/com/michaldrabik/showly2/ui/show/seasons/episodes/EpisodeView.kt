package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.utilities.extensions.addRipple
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_episode.view.*

class EpisodeView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_episode, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
  }

  fun bind(episode: Episode, itemClickListener: (Episode) -> Unit) {
    episodeTitle.text = context.getString(R.string.textEpisode, episode.number)
    episodeOverview.text = episode.title
    onClick { itemClickListener(episode) }
  }
}