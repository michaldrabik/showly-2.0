package com.michaldrabik.showly2.ui.show

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.SeasonBundle
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.FollowedState
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodeListItem
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesInteractor
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor,
  private val episodesInteractor: EpisodesInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private val seasonItems = mutableListOf<SeasonListItem>()

  fun loadShowDetails(id: Long) {
    viewModelScope.launch {
      try {
        uiStream.value = ShowDetailsUiModel(showLoading = true)
        show = interactor.loadShowDetails(id)
        val isFollowed = async { interactor.isFollowed(show) }
        val isWatchLater = async { interactor.isWatchLater(show) }
        val followedState = FollowedState(
          isMyShows = isFollowed.await(),
          isWatchLater = isWatchLater.await(),
          withAnimation = false
        )
        uiStream.value = ShowDetailsUiModel(
          show = show,
          showLoading = false,
          isFollowed = followedState
        )

        launch { loadNextEpisode(show) }
        launch { loadBackgroundImage(show) }
        launch { loadActors(show) }
        launch {
          areSeasonsLoaded = false
          val seasons = loadSeasons(show)
          if (followedState.isMyShows) episodesInteractor.invalidateEpisodes(show, seasons)
          areSeasonsLoaded = true
        }
        launch { loadRelatedShows(show) }
      } catch (t: Throwable) {
        uiStream.value = ShowDetailsUiModel(error = Error(t))
      }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = interactor.loadNextEpisode(show.id)
      uiStream.value = ShowDetailsUiModel(nextEpisode = episode)
    } catch (t: Throwable) {
      //NOOP
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    try {
      val backgroundImage = interactor.loadBackgroundImage(show)
      uiStream.value = ShowDetailsUiModel(image = backgroundImage)
    } catch (t: Throwable) {
      uiStream.value = ShowDetailsUiModel(image = Image.createUnavailable(FANART))
    }
  }

  private suspend fun loadActors(show: Show) {
    try {
      val actors = interactor.loadActors(show)
      uiStream.value = ShowDetailsUiModel(actors = actors)
    } catch (t: Throwable) {
      uiStream.value = ShowDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadSeasons(show: Show): List<Season> = try {
    val seasons = interactor.loadSeasons(show)
    val seasonsItems = seasons.map {
      val episodes = it.episodes.map { episode -> EpisodeListItem(episode, false) }
      SeasonListItem(it, episodes, false)
    }
    val calculated = calculateWatchedEpisodes(seasonsItems)
    uiStream.value = ShowDetailsUiModel(seasons = calculated)
    seasons
  } catch (t: Throwable) {
    uiStream.value = ShowDetailsUiModel(seasons = emptyList())
    emptyList()
  }

  private suspend fun loadRelatedShows(show: Show) {
    try {
      val relatedShows = interactor.loadRelatedShows(show).map {
        val image = interactor.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      uiStream.value = ShowDetailsUiModel(relatedShows = relatedShows)
    } catch (t: Throwable) {
      uiStream.value = ShowDetailsUiModel(relatedShows = emptyList())
    }
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          ShowDetailsUiModel(updateRelatedShow = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun addFollowedShow() {
    if (!areSeasonsLoaded) {
      uiStream.value = ShowDetailsUiModel(info = R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }
      interactor.addToFollowed(show, seasons, episodes)

      val state = FollowedState(isMyShows = true, isWatchLater = false, withAnimation = true)
      uiStream.value = ShowDetailsUiModel(isFollowed = state)
    }
  }

  fun addWatchLaterShow() {
    if (!areSeasonsLoaded) {
      uiStream.value = ShowDetailsUiModel(info = R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
      interactor.addToWatchLater(show)

      val state = FollowedState(isMyShows = false, isWatchLater = true, withAnimation = true)
      uiStream.value = ShowDetailsUiModel(isFollowed = state)
    }
  }

  fun removeFromFollowed() {
    if (!areSeasonsLoaded) {
      uiStream.value = ShowDetailsUiModel(info = R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
      val isFollowed = interactor.isFollowed(show)
      val isWatchLater = interactor.isWatchLater(show)

      if (isFollowed) interactor.removeFromFollowed(show)
      if (isWatchLater) interactor.removeFromWatchLater(show)

      val state = FollowedState(isMyShows = false, isWatchLater = false, withAnimation = true)
      uiStream.value = ShowDetailsUiModel(isFollowed = state)
    }
  }

  fun setWatchedEpisode(episode: Episode, season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      when {
        isChecked -> episodesInteractor.setEpisodeWatched(bundle)
        else -> episodesInteractor.setEpisodeUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  fun setWatchedSeason(season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = SeasonBundle(season, show)
      when {
        isChecked -> episodesInteractor.setSeasonWatched(bundle)
        else -> episodesInteractor.setSeasonUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  private suspend fun refreshWatchedEpisodes() {
    val updatedSeasonItems = calculateWatchedEpisodes(this.seasonItems)
    uiStream.value = ShowDetailsUiModel(seasons = updatedSeasonItems)
  }

  private suspend fun calculateWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> {
    val items = mutableListOf<SeasonListItem>()

    val watchedSeasonsIds = episodesInteractor.getWatchedSeasonsIds(show)
    val watchedEpisodesIds = episodesInteractor.getWatchedEpisodesIds(show)

    seasonsList.forEach { item ->
      val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
      val episodes = item.episodes.map { episodeItem ->
        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
        EpisodeListItem(episodeItem.episode, isEpisodeWatched)
      }
      val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
      items.add(updated)
    }

    seasonItems.run {
      clear()
      addAll(items)
    }

    return items
  }
}
