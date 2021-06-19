package com.michaldrabik.ui_show.episodes

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.seasons.SeasonListItem
import kotlinx.android.synthetic.main.view_episodes.view.*
import java.util.Locale.ENGLISH

class EpisodesView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemClickListener: (Show, Episode, Season, Boolean) -> Unit = { _, _, _, _ -> }
  var itemCheckedListener: (Episode, Season, Boolean) -> Unit = { _, _, _ -> }
  var seasonCheckedListener: (Season, Boolean) -> Unit = { _, _ -> }

  private val episodesAdapter by lazy { EpisodesAdapter() }
  private lateinit var show: Show
  private lateinit var season: Season
  private lateinit var episodes: List<EpisodeListItem>
  private var isLocked = true

  init {
    inflate(context, R.layout.view_episodes, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  private fun toggleEpisodesLock() {
    isLocked = !isLocked
    episodesUnlockButton.setImageResource(if (isLocked) R.drawable.ic_locked else R.drawable.ic_unlocked)
    episodesCheckbox.isEnabled = !isLocked
    episodesAdapter.toggleEpisodesLock()
  }

  fun bind(seasonItem: SeasonListItem) {
    clear()
    this.show = seasonItem.show.copy()
    this.season = seasonItem.season.copy()
    this.episodes = seasonItem.episodes.toList()
    episodesTitle.text =
      if (seasonItem.season.isSpecial()) context.getString(R.string.textSpecials)
      else String.format(ENGLISH, context.getString(R.string.textSeason), season.number)
    episodesOverview.text = season.overview
    episodesOverview.visibleIf(season.overview.isNotBlank())
    episodesCheckbox.run {
      isEnabled = seasonItem.episodes.all { it.episode.hasAired(season) } || !isLocked
      setCheckedSilent(seasonItem.isWatched) { _, isChecked ->
        seasonCheckedListener(season, isChecked)
      }
      jumpDrawablesToCurrentState()
    }
    episodesUnlockButton.visibleIf(!seasonItem.season.isSpecial() && seasonItem.episodes.any { !it.episode.hasAired(season) })
    episodesUnlockButton.onClick(safe = false) { toggleEpisodesLock() }
  }

  fun bindEpisodes(episodes: List<EpisodeListItem>, animate: Boolean = true) {
    episodesAdapter.setItems(episodes)
    if (animate) episodesRecycler.scheduleLayoutAnimation()
  }

  fun updateEpisodes(seasonListItems: List<SeasonListItem>) {
    if (!this::season.isInitialized) return
    val seasonListItem = seasonListItems.find { it.season.ids.trakt == season.ids.trakt }
    seasonListItem?.let {
      this.season = it.season.copy()
      episodesCheckbox.setCheckedSilent(it.isWatched) { _, isChecked ->
        seasonCheckedListener(season, isChecked)
      }
      episodesAdapter.setItems(it.episodes)
    }
  }

  fun selectEpisode(episode: Episode) {
    val item = episodes.find { it.episode.number == episode.number }
    if (item != null) {
      itemClickListener.invoke(show, item.episode, season, item.isWatched)
    }
  }

  private fun setupRecycler() {
    episodesRecycler.apply {
      setHasFixedSize(true)
      adapter = episodesAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
    }
    episodesAdapter.itemClickListener = { episode, isWatched -> itemClickListener(show, episode, season, isWatched) }
    episodesAdapter.itemCheckedListener = { episode, isChecked -> itemCheckedListener(episode, season, isChecked) }
  }

  private fun clear() {
    isLocked = true
    episodesUnlockButton.setOnClickListener(null)
    episodesUnlockButton.setImageResource(R.drawable.ic_locked)
    episodesUnlockButton.gone()
    episodesAdapter.clearItems()
  }
}
