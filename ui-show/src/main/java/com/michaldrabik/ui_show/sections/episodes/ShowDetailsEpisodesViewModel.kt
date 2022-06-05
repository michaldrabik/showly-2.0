package com.michaldrabik.ui_show.sections.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.sections.episodes.ShowDetailsEpisodesFragment.Options
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesAnnouncementsCase
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesLoadShowCase
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesRatingCase
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesSetWatchedCase
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesSetWatchedCase.Result
import com.michaldrabik.ui_show.sections.episodes.cases.EpisodesTranslationCase
import com.michaldrabik.ui_show.sections.episodes.recycler.EpisodeListItem
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@HiltViewModel
class ShowDetailsEpisodesViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val loadShowCase: EpisodesLoadShowCase,
  private val setWatchedCase: EpisodesSetWatchedCase,
  private val ratingsCase: EpisodesRatingCase,
  private val translationCase: EpisodesTranslationCase,
  private val announcementsCase: EpisodesAnnouncementsCase,
  private val episodesManager: EpisodesManager,
  private val seasonsCache: SeasonsCache
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val initialLoadState = MutableStateFlow<Boolean?>(null)
  private val showState = MutableStateFlow<Show?>(null)
  private val seasonState = MutableStateFlow<SeasonListItem?>(null)
  private val episodesState = MutableStateFlow<List<EpisodeListItem>?>(null)

  private var show by notNull<Show>()
  private var season by notNull<SeasonListItem>()

  init {
    val (showId, seasonId) = savedStateHandle.get<Options>(ARG_OPTIONS)!!
    loadInitialData(showId, seasonId)
  }

  private fun loadInitialData(showId: IdTrakt, seasonId: IdTrakt) {
    viewModelScope.launch {
      this@ShowDetailsEpisodesViewModel.show = loadShowCase.loadDetails(showId)
      showState.value = show

      val seasons = seasonsCache.loadSeasons(showId)
      val season = seasons?.find { it.id == seasonId.id }
      if (seasons == null || season == null) {
        eventChannel.send(ShowDetailsEpisodesEvent.Finish)
        return@launch
      }
      this@ShowDetailsEpisodesViewModel.season = season
      seasonState.value = season

      delay(265) // Let enter transition animation complete peacefully.
      episodesState.value = season.episodes
      initialLoadState.value = true

      loadTranslations(season)
    }
  }

  private fun loadTranslations(season: SeasonListItem) {
    viewModelScope.launch {
      try {
        val translations = translationCase.loadTranslations(season.season, show)
        val episodes = episodesState.value?.toMutableList() ?: mutableListOf()

        if (translations.isEmpty() || episodes.isEmpty()) {
          return@launch
        }

        translations.forEach { translation ->
          val episode = episodes.find { it.id == translation.ids.trakt.id }
          episode?.let { ep ->
            if (translation.title.isNotBlank() || translation.overview.isNotBlank()) {
              val t = Translation(translation.title, translation.overview, translation.language)
              val withTranslation = ep.copy(translation = t)
              episodes.findReplace(withTranslation) { it.id == withTranslation.id }
            }
          }
        }

        val updatedSeason = season.copy(episodes = episodes, updatedAt = nowUtcMillis())
        val calculated = markWatchedEpisodes(updatedSeason)
        seasonState.value = calculated
        episodesState.value = calculated.episodes
        initialLoadState.value = false
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ShowDetailsEpisodesViewModel::loadSeasonTranslation()")
        Timber.w(error)
        rethrowCancellation(error)
      }
    }
  }

  fun setEpisodeWatched(
    episode: Episode,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season.season, show)
      val result = setWatchedCase.setEpisodeWatched(bundle, isChecked)
      if (result == Result.REMOVE_FROM_TRAKT) {
        val ids = listOf(episode.ids.trakt)
        val mode = RemoveTraktBottomSheet.Mode.EPISODE
        eventChannel.send(ShowDetailsEpisodesEvent.RemoveFromTrakt(R.id.actionEpisodesFragmentToRemoveTraktProgress, mode, ids))
      }
      refreshWatchedEpisodes()
      announcementsCase.refreshAnnouncements(show.ids.trakt)
    }
  }

  fun openEpisodeDetails(episode: Episode) {
    val isWatched = episodesState.value?.find { it.id == episode.ids.trakt.id }?.isWatched!!
    openEpisodeDetails(episode, isWatched)
  }

  fun openEpisodeDetails(episode: Episode, isWatched: Boolean) {
    viewModelScope.launch {
      val episodeBundle = EpisodeBundle(
        episode = episode,
        season = season.season,
        show = show
      )
      eventChannel.send(ShowDetailsEpisodesEvent.OpenEpisodeDetails(episodeBundle, isWatched))
    }
  }

  fun refreshEpisodesRatings() {
    viewModelScope.launch {
      val seasonItem = seasonState.value
      val episodeItems = episodesState.value ?: emptyList()

      seasonItem?.let {
        val ratingSeason = ratingsCase.loadRating(seasonItem.season)
        val updatedEpisodesItems = episodeItems.map { episodeItem ->
          async {
            val ratingEpisode = ratingsCase.loadRating(episodeItem.episode)
            episodeItem.copy(myRating = ratingEpisode)
          }
        }.awaitAll()
        val updatedSeasonItem = seasonItem.copy(episodes = updatedEpisodesItems, userRating = seasonItem.userRating.copy(ratingSeason))
        seasonState.value = updatedSeasonItem
        episodesState.value = updatedEpisodesItems
        initialLoadState.value = false
      }
    }
  }

  fun launchRefreshWatchedEpisodes() {
    viewModelScope.launch {
      refreshWatchedEpisodes()
    }
  }

  private suspend fun refreshWatchedEpisodes() {
    val season = seasonState.value?.copy()
    season?.let {
      val calculated = markWatchedEpisodes(it)
      seasonState.value = calculated
      episodesState.value = calculated.episodes
      initialLoadState.value = false
    }
  }

  private suspend fun markWatchedEpisodes(season: SeasonListItem): SeasonListItem =
    coroutineScope {
      val (watchedSeasonsIds, watchedEpisodesIds) = awaitAll(
        async { episodesManager.getWatchedSeasonsIds(show) },
        async { episodesManager.getWatchedEpisodesIds(show) }
      )

      val isSeasonWatched = watchedSeasonsIds.any { id -> id == season.id }
      val episodes = season.episodes.map { episodeItem ->
        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
        episodeItem.copy(season = season.season, isWatched = isEpisodeWatched)
      }

      season.copy(episodes = episodes, isWatched = isSeasonWatched)
    }

  private fun refreshSeasonsCache() {
    val cachedSeasons = seasonsCache.loadSeasons(show.ids.trakt)?.toMutableList()
    val currentSeason = seasonState.value
    val currentEpisodes = episodesState.value
    val isSeasonLocal = seasonsCache.areSeasonsLocal(show.ids.trakt)

    if (currentSeason != null && currentEpisodes != null) {
      cachedSeasons?.find { it.id == currentSeason.id }?.let { season ->
        val updated = currentSeason.copy(episodes = currentEpisodes)
        cachedSeasons.findReplace(updated) { it.id == season.id }
        seasonsCache.setSeasons(show.ids.trakt, cachedSeasons, isSeasonLocal)
      }
    }
  }

  override fun onCleared() {
    refreshSeasonsCache()
    super.onCleared()
  }

  val uiState = combine(
    showState,
    seasonState,
    episodesState,
    initialLoadState
  ) { s1, s2, s3, s4 ->
    ShowDetailsEpisodesUiState(
      show = s1,
      season = s2,
      episodes = s3,
      isInitialLoad = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsEpisodesUiState()
  )
}
