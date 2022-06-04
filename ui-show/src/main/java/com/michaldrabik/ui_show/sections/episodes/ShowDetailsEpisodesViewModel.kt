package com.michaldrabik.ui_show.sections.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import com.michaldrabik.ui_show.sections.episodes.ShowDetailsEpisodesFragment.Options
import com.michaldrabik.ui_show.sections.episodes.recycler.EpisodeListItem
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@HiltViewModel
class ShowDetailsEpisodesViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val seasonsCache: SeasonsCache
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val showState = MutableStateFlow<Show?>(null)
  private val seasonState = MutableStateFlow<SeasonListItem?>(null)
  private val episodesState = MutableStateFlow<List<EpisodeListItem>?>(null)

  private var showId by notNull<IdTrakt>()
  private var seasonId by notNull<IdTrakt>()

  init {
    val (showId, seasonId) = savedStateHandle.get<Options>(ARG_OPTIONS)!!
    this.showId = showId
    this.seasonId = seasonId
    loadEpisodes()
  }

  private fun loadEpisodes() {
    viewModelScope.launch {
      val seasons = seasonsCache.loadSeasons(showId)
      if (seasons == null) {
        eventChannel.send(ShowDetailsEpisodesEvent.Finish)
        return@launch
      }
      val season = seasons.find { it.id == seasonId.id }
      seasonState.value = season

      delay(265) // Let enter transition animation complete peacefully.
      episodesState.value = season?.episodes
    }
  }

  fun setEpisodeWatched(
    episode: Episode,
    season: SeasonListItem,
    isChecked: Boolean,
    removeTrakt: Boolean
  ) {
    TODO("Not yet implemented")
  }

  val uiState = combine(
    showState,
    seasonState,
    episodesState
  ) { s1, s2, s3 ->
    ShowDetailsEpisodesUiState(
      show = s1,
      season = s2,
      episodes = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsEpisodesUiState()
  )
}
