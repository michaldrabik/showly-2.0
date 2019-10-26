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
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem
import com.michaldrabik.showly2.utilities.extensions.setCheckedSilent
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_episodes.view.*

class EpisodesView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemClickListener: (Episode, Season, Boolean) -> Unit = { _, _, _ -> }
  var itemCheckedListener: (Episode, Season, Boolean) -> Unit = { _, _, _ -> }
  var seasonCheckedListener: (Season, Boolean) -> Unit = { _, _ -> }

  private val episodesAdapter by lazy { EpisodesAdapter() }
  private lateinit var season: Season

  init {
    inflate(context, R.layout.view_episodes, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  fun bind(seasonItem: SeasonListItem) {
    clear()
    this.season = seasonItem.season.copy()
    episodesTitle.text = context.getString(R.string.textSeason, season.number)
    episodesOverview.text = season.overview
    episodesOverview.visibleIf(season.overview.isNotBlank())
    episodesCheckbox.run {
      isEnabled = seasonItem.episodes.all { it.episode.hasAired(season) }
      setCheckedSilent(seasonItem.isWatched) { _, isChecked ->
        seasonCheckedListener(season, isChecked)
      }
      jumpDrawablesToCurrentState()
    }
  }

  fun bindEpisodes(episodes: List<EpisodeListItem>) {
    episodesAdapter.setItems(episodes)
    episodesRecycler.scheduleLayoutAnimation()
  }

  fun updateEpisodes(seasonListItems: List<SeasonListItem>) {
    if (!this::season.isInitialized) return
    val seasonListItem = seasonListItems.find { it.season.id == season.id }
    seasonListItem?.let {
      this.season = it.season.copy()
      episodesCheckbox.setCheckedSilent(it.isWatched) { _, isChecked ->
        seasonCheckedListener(season, isChecked)
      }
      episodesAdapter.setItems(it.episodes)
    }
  }

  private fun setupRecycler() {
    episodesRecycler.apply {
      setHasFixedSize(true)
      adapter = episodesAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
    }
    episodesAdapter.itemClickListener = { episode, isWatched -> itemClickListener(episode, season, isWatched) }
    episodesAdapter.itemCheckedListener = { episode, isChecked -> itemCheckedListener(episode, season, isChecked) }
  }

  private fun clear() = episodesAdapter.clearItems()
}