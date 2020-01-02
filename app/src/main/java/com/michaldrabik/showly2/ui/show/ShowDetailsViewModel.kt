package com.michaldrabik.showly2.ui.show

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.IdTrakt
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
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.showly2.utilities.extensions.findReplace
import com.michaldrabik.showly2.utilities.extensions.replace
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor,
  private val episodesManager: EpisodesManager,
  private val announcementManager: AnnouncementManager
) : BaseViewModel<ShowDetailsUiModel>() {

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private val seasonItems = mutableListOf<SeasonListItem>()

  fun loadShowDetails(id: IdTrakt, context: Context) {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showLoading = true)
        show = interactor.loadShowDetails(id)
        val isFollowed = async { interactor.isFollowed(show) }
        val isWatchLater = async { interactor.isWatchLater(show) }
        val followedState = FollowedState(
          isMyShows = isFollowed.await(),
          isWatchLater = isWatchLater.await(),
          withAnimation = false
        )

        uiState = ShowDetailsUiModel(show = show, showLoading = false, isFollowed = followedState)

        launch { loadNextEpisode(show) }
        launch { loadBackgroundImage(show) }
        launch { loadActors(show) }
        launch {
          areSeasonsLoaded = false
          val seasons = loadSeasons(show)
          if (followedState.isMyShows) {
            episodesManager.invalidateEpisodes(show, seasons)
            announcementManager.refreshEpisodesAnnouncements(context)
          }
          areSeasonsLoaded = true
        }
        launch { loadComments(show) }
        launch { loadRelatedShows(show) }
      } catch (t: Throwable) {
        _errorStream.value = R.string.errorCouldNotLoadShow
      }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = interactor.loadNextEpisode(show.ids.trakt)
      uiState = ShowDetailsUiModel(nextEpisode = episode)
    } catch (t: Throwable) {
      // NOOP
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    uiState = try {
      val backgroundImage = interactor.loadBackgroundImage(show)
      ShowDetailsUiModel(image = backgroundImage)
    } catch (t: Throwable) {
      ShowDetailsUiModel(image = Image.createUnavailable(FANART))
    }
  }

  private suspend fun loadActors(show: Show) {
    uiState = try {
      val actors = interactor.loadActors(show)
      ShowDetailsUiModel(actors = actors)
    } catch (t: Throwable) {
      ShowDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadSeasons(show: Show): List<Season> = try {
    val seasons = interactor.loadSeasons(show)
    val seasonsItems = seasons.map {
      val episodes = it.episodes.map { episode -> EpisodeListItem(episode, it, false) }
      SeasonListItem(it, episodes, false)
    }
    val calculated = markWatchedEpisodes(seasonsItems)
    uiState = ShowDetailsUiModel(seasons = calculated)
    seasons
  } catch (t: Throwable) {
    uiState = ShowDetailsUiModel(seasons = emptyList())
    emptyList()
  }

  private suspend fun loadComments(show: Show) {
    uiState = try {
      val comments = interactor.loadComments(show)
      ShowDetailsUiModel(comments = comments)
    } catch (t: Throwable) {
      ShowDetailsUiModel(comments = emptyList())
    }
  }

  private suspend fun loadRelatedShows(show: Show) {
    uiState = try {
      val relatedShows = interactor.loadRelatedShows(show).map {
        val image = interactor.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      ShowDetailsUiModel(relatedShows = relatedShows)
    } catch (t: Throwable) {
      ShowDetailsUiModel(relatedShows = emptyList())
    }
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState?.relatedShows?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(relatedShows = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun addFollowedShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }
      interactor.addToFollowed(show, seasons, episodes)

      val followedState = FollowedState(isMyShows = true, isWatchLater = false, withAnimation = true)
      uiState = ShowDetailsUiModel(isFollowed = followedState)

      announcementManager.refreshEpisodesAnnouncements(context)
    }
  }

  fun addWatchLaterShow() {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      interactor.addToWatchLater(show)

      val followedState = FollowedState(isMyShows = false, isWatchLater = true, withAnimation = true)
      uiState = ShowDetailsUiModel(isFollowed = followedState)
    }
  }

  fun removeFromFollowed(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val isFollowed = interactor.isFollowed(show)
      val isWatchLater = interactor.isWatchLater(show)

      if (isFollowed) interactor.removeFromFollowed(show)
      if (isWatchLater) interactor.removeFromWatchLater(show)

      val followedState = FollowedState(isMyShows = false, isWatchLater = false, withAnimation = true)
      uiState = ShowDetailsUiModel(isFollowed = followedState)

      announcementManager.refreshEpisodesAnnouncements(context)
    }
  }

  fun setWatchedEpisode(episode: Episode, season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      when {
        isChecked -> episodesManager.setEpisodeWatched(bundle)
        else -> episodesManager.setEpisodeUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  fun setWatchedSeason(season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = SeasonBundle(season, show)
      when {
        isChecked -> episodesManager.setSeasonWatched(bundle)
        else -> episodesManager.setSeasonUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  private fun checkSeasonsLoaded(): Boolean {
    if (!areSeasonsLoaded) {
      _messageStream.value = R.string.errorSeasonsNotLoaded
      return false
    }
    return true
  }

  private suspend fun refreshWatchedEpisodes() {
    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
    uiState = ShowDetailsUiModel(seasons = updatedSeasonItems)
  }

  private suspend fun markWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> {
    val items = mutableListOf<SeasonListItem>()

    val watchedSeasonsIds = episodesManager.getWatchedSeasonsIds(show)
    val watchedEpisodesIds = episodesManager.getWatchedEpisodesIds(show)

    seasonsList.forEach { item ->
      val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
      val episodes = item.episodes.map { episodeItem ->
        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
        EpisodeListItem(episodeItem.episode, item.season, isEpisodeWatched)
      }
      val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
      items.add(updated)
    }

    seasonItems.replace(items)
    return items
  }
}
