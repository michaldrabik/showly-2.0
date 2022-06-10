package com.michaldrabik.ui_show.sections.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsEvent
import com.michaldrabik.ui_show.episodes.cases.EpisodesMarkWatchedCase
import com.michaldrabik.ui_show.quicksetup.QuickSetupListItem
import com.michaldrabik.ui_show.sections.seasons.cases.ShowDetailsLoadSeasonsCase
import com.michaldrabik.ui_show.sections.seasons.cases.ShowDetailsQuickProgressCase
import com.michaldrabik.ui_show.sections.seasons.cases.ShowDetailsWatchedSeasonCase
import com.michaldrabik.ui_show.sections.seasons.cases.ShowDetailsWatchedSeasonCase.Result
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailsSeasonsViewModel @Inject constructor(
  private val loadSeasonsCase: ShowDetailsLoadSeasonsCase,
  private val quickProgressCase: ShowDetailsQuickProgressCase,
  private val watchedSeasonCase: ShowDetailsWatchedSeasonCase,
  private val markWatchedCase: EpisodesMarkWatchedCase,
  private val seasonsCache: SeasonsCache,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var show: Show

  private val loadingState = MutableStateFlow(true)
  private val seasonsState = MutableStateFlow<List<SeasonListItem>?>(null)

  private var areSeasonsLocal = false

  fun handleEvent(event: ShowDetailsEvent<*>) {
    when (event) {
      is ShowDetailsEvent.RefreshSeasons -> refreshSeasons()
      else -> Unit
    }
  }

  fun loadSeasons(show: Show) {
    if (this::show.isInitialized) return
    this.show = show
    viewModelScope.launch {
      try {
        val (seasons, isLocal) = loadSeasonsCase.loadSeasons(show)
        areSeasonsLocal = isLocal
        val calculated = markWatchedCase.markWatchedEpisodes(show, seasons)
        updateSeasons(calculated)
      } catch (error: Throwable) {
        updateSeasons(emptyList())
      }
    }
  }

  fun setSeasonWatched(
    season: Season,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      val result = watchedSeasonCase.setSeasonWatched(
        show = show,
        season = season,
        isChecked = isChecked,
        isLocal = areSeasonsLocal
      )
      if (result == Result.REMOVE_FROM_TRAKT) {
        val ids = season.episodes.map { it.ids.trakt }
        val event = ShowDetailsSeasonsEvent.RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktProgress, Mode.EPISODE, ids)
        eventChannel.send(event)
      }
      refreshSeasons()
    }
  }

  fun setQuickProgress(item: QuickSetupListItem?) {
    viewModelScope.launch {
      if (item == null || !checkSeasonsLoaded()) {
        return@launch
      }

      val seasonItems = seasonsState.value?.toList() ?: emptyList()
      quickProgressCase.setQuickProgress(item, seasonItems, show)
      refreshSeasons()

      messageChannel.send(MessageEvent.Info(R.string.textShowQuickProgressDone))
      Analytics.logShowQuickProgress(show)
    }
  }

  fun openSeasonEpisodes(season: SeasonListItem) {
    viewModelScope.launch {
      seasonsCache.setSeasons(show.ids.trakt, seasonsState.value ?: emptyList(), areSeasonsLocal)
      val event = ShowDetailsSeasonsEvent.OpenSeasonEpisodes(show.ids.trakt, season.season.ids.trakt)
      eventChannel.send(event)
    }
  }

  fun refreshSeasons() {
    if (!this::show.isInitialized || seasonsState.value == null) {
      return
    }
    viewModelScope.launch {
      val seasonItems = seasonsState.value?.toList() ?: emptyList()
      val calculated = markWatchedCase.markWatchedEpisodes(show, seasonItems)
      updateSeasons(calculated)
    }
  }

  private suspend fun checkSeasonsLoaded(): Boolean {
    if (seasonsState.value == null) {
      messageChannel.send(MessageEvent.Info(R.string.errorSeasonsNotLoaded))
      return false
    }
    return true
  }

  private fun updateSeasons(seasons: List<SeasonListItem>) {
    seasonsState.value = seasons
    seasonsCache.setSeasons(show.ids.trakt, seasons, areSeasonsLocal)
  }

  val uiState = combine(
    loadingState,
    seasonsState
  ) { s1, s2 ->
    ShowDetailsSeasonsUiState(
      isLoading = s1,
      seasons = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsSeasonsUiState()
  )
}
