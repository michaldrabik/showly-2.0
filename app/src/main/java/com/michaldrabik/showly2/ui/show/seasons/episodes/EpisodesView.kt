package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_episodes.view.*

class EpisodesView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), (Episode) -> Unit {

  var itemClickListener: (Episode) -> Unit = {}
  var itemCheckedListener: (Episode, Season) -> Unit = { _, _ -> }

  private val episodesAdapter by lazy { EpisodesAdapter() }
  private lateinit var season: Season

  init {
    inflate(context, R.layout.view_episodes, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  fun bind(season: Season) {
    this.season = season.copy()
    episodesAdapter.clearItems()
    episodesTitle.text = context.getString(R.string.textSeason, season.number)
    episodesOverview.text = season.overview
    episodesOverview.visibleIf(season.overview.isNotBlank())
  }

  fun bindEpisodes(episodes: List<Episode>) {
    episodesAdapter.setItems(episodes)
    episodesRecycler.scheduleLayoutAnimation()
  }

  private fun setupRecycler() {
    episodesRecycler.apply {
      setHasFixedSize(true)
      adapter = episodesAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }
    episodesAdapter.itemClickListener = { itemClickListener(it) }
    episodesAdapter.itemCheckedListener = this
  }

  override fun invoke(episode: Episode) = itemCheckedListener(episode, season)
}