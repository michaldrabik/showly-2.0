package com.michaldrabik.ui_show.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.extensions.nowUtcMillis
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
import com.michaldrabik.ui_show.episodes.ShowDetailsEpisodesFragment.Options
import com.michaldrabik.ui_show.episodes.cases.EpisodesAnnouncementsCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesLoadShowCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesMarkWatchedCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesRatingCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesSetEpisodeWatchedCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesSetEpisodeWatchedCase.Result
import com.michaldrabik.ui_show.episodes.cases.EpisodesSetSeasonWatchedCase
import com.michaldrabik.ui_show.episodes.cases.EpisodesSetSeasonWatchedCase.Result.REMOVE_FROM_TRAKT
import com.michaldrabik.ui_show.episodes.cases.EpisodesTranslationCase
import com.michaldrabik.ui_show.episodes.recycler.EpisodeListItem
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
  private val episodeWatchedCase: EpisodesSetEpisodeWatchedCase,
  private val seasonWatchedCase: EpisodesSetSeasonWatchedCase,
  private val ratingsCase: EpisodesRatingCase,
  private val translationCase: EpisodesTranslationCase,
  private val announcementsCase: EpisodesAnnouncementsCase,
  private val markWatchedCase: EpisodesMarkWatchedCase,
  private val seasonsCache: SeasonsCache,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val seasonState = MutableStateFlow<SeasonListItem?>(null)
  private val episodesState = MutableStateFlow<List<EpisodeListItem>?>(null)
  private val initialLoadState = MutableStateFlow<Boolean?>(null)

  private var show by notNull<Show>()

  init {
    val (showId, seasonId) = savedStateHandle.get<Options>(ARG_OPTIONS)!!
    loadInitialData(showId, seasonId)
  }

  private fun loadInitialData(showId: IdTrakt, seasonId: IdTrakt) {
    viewModelScope.launch {
      this@ShowDetailsEpisodesViewModel.show = loadShowCase.loadDetails(showId)

      val seasons = seasonsCache.loadSeasons(showId)
      val season = seasons?.find { it.id == seasonId.id }
      if (seasons == null || season == null) {
        eventChannel.send(ShowDetailsEpisodesEvent.Finish)
        return@launch
      }
      val ratingSeason = ratingsCase.loadRating(season.season)
      seasonState.value = season.copy(
        userRating = season.userRating.copy(ratingSeason)
      )

      delay(265) // Let enter transition animation complete peacefully.
      episodesState.value = season.episodes
      initialLoadState.value = true

      loadEpisodesRating().join()
      loadTranslations()
    }
  }

  fun loadEpisodesRating(): Job =
    viewModelScope.launch {
      val seasonItem = seasonState.value
      val episodeItems = episodesState.value ?: emptyList()

      seasonItem?.let {
        val updatedEpisodesItems = episodeItems.map { episodeItem ->
          async {
            val ratingEpisode = ratingsCase.loadRating(episodeItem.episode)
            episodeItem.copy(myRating = ratingEpisode)
          }
        }.awaitAll()
        val updatedSeasonItem = seasonItem.copy(
          episodes = updatedEpisodesItems
        )
        seasonState.value = updatedSeasonItem
        episodesState.value = updatedEpisodesItems
        initialLoadState.value = false
      }
    }

  fun loadSeasonRating() {
    viewModelScope.launch {
      val seasonItem = seasonState.value
      seasonItem?.let {
        val ratingSeason = ratingsCase.loadRating(seasonItem.season)
        val updatedSeasonItem = seasonItem.copy(
          userRating = seasonItem.userRating.copy(ratingSeason)
        )
        seasonState.value = updatedSeasonItem
        initialLoadState.value = false
      }
    }
  }

  private suspend fun loadTranslations() {
    try {
      val season = seasonState.value?.season
      val episodes = episodesState.value?.toMutableList() ?: mutableListOf()
      val translations = translationCase.loadTranslations(season, show)

      if (translations.isEmpty() || episodes.isEmpty()) {
        return
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

      val updatedSeason = seasonState.value?.copy(episodes = episodes, updatedAt = nowUtcMillis())
      updatedSeason?.let {
        val marked = markWatchedCase.markWatchedEpisodes(show, updatedSeason)
        seasonState.value = marked
        episodesState.value = marked.episodes
        initialLoadState.value = false
      }
    } catch (error: Throwable) {
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  fun setEpisodeWatched(
    episode: Episode,
    isChecked: Boolean,
  ) {
    viewModelScope.launch {
      seasonState.value?.let {
        val bundle = EpisodeBundle(episode, it.season, show)
        val result = episodeWatchedCase.setEpisodeWatched(bundle, isChecked)
        if (result == Result.REMOVE_FROM_TRAKT) {
          val event = ShowDetailsEpisodesEvent.RemoveFromTrakt(
            actionId = R.id.actionEpisodesFragmentToRemoveTraktProgress,
            mode = RemoveTraktBottomSheet.Mode.EPISODE,
            traktIds = listOf(episode.ids.trakt)
          )
          eventChannel.send(event)
        }
        refreshWatchedEpisodes()
        announcementsCase.refreshAnnouncements(show.ids.trakt)
      }
    }
  }

  fun setSeasonWatched(season: SeasonListItem, isChecked: Boolean) {
    viewModelScope.launch {
      val result = seasonWatchedCase.setSeasonWatched(show, season.season, isChecked)
      if (result == REMOVE_FROM_TRAKT) {
        val event = ShowDetailsEpisodesEvent.RemoveFromTrakt(
          actionId = R.id.actionEpisodesFragmentToRemoveTraktProgress,
          mode = RemoveTraktBottomSheet.Mode.EPISODE,
          traktIds = season.season.episodes.map { it.ids.trakt }
        )
        eventChannel.send(event)
      }
      refreshWatchedEpisodes()
      announcementsCase.refreshAnnouncements(show.ids.trakt)
    }
  }

  fun openEpisodeDetails(episode: Episode) {
    val currentEpisode = episodesState.value?.find { it.season.number == episode.season && it.episode.number == episode.number }!!
    openEpisodeDetails(episode, currentEpisode.isWatched)
  }

  fun openEpisodeDetails(episode: Episode, isWatched: Boolean) {
    val currentSeason = seasonState.value?.season
    val currentEpisode = episodesState.value
      ?.find { it.season.number == episode.season && it.episode.number == episode.number }

    if (currentSeason == null || currentEpisode == null) {
      return
    }

    viewModelScope.launch {
      val episodeBundle = EpisodeBundle(
        episode = currentEpisode.episode,
        season = currentSeason,
        show = show
      )
      eventChannel.send(ShowDetailsEpisodesEvent.OpenEpisodeDetails(episodeBundle, isWatched))
    }
  }

  fun openRateSeasonDialog() {
    viewModelScope.launch {
      seasonState.value?.season?.let {
        eventChannel.send(ShowDetailsEpisodesEvent.OpenRateSeason(it))
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
      val marked = markWatchedCase.markWatchedEpisodes(show, it)
      seasonState.value = marked
      episodesState.value = marked.episodes
      initialLoadState.value = false
    }
  }

  private fun refreshSeasonsCache() {
    val cachedSeasons = seasonsCache.loadSeasons(show.ids.trakt)?.toMutableList()
    val currentSeason = seasonState.value
    val currentEpisodes = episodesState.value
    val isSeasonLocal = seasonsCache.areSeasonsLocal(show.ids.trakt)

    if (currentSeason != null && currentEpisodes != null) {
      cachedSeasons?.find { it.id == currentSeason.id }?.let { season ->
        val updated = currentSeason.copy(
          episodes = currentEpisodes,
          userRating = currentSeason.userRating
        )
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
    seasonState,
    episodesState,
    initialLoadState
  ) { s2, s3, s4 ->
    ShowDetailsEpisodesUiState(
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
