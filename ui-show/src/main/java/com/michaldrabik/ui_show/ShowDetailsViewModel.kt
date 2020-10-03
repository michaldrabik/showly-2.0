package com.michaldrabik.ui_show

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsActorsCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsArchiveCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsCommentsCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsEpisodesCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsMainCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsRatingCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsRelatedShowsCase
import com.michaldrabik.showly2.ui.show.cases.ShowDetailsSeeLaterCase
import com.michaldrabik.showly2.ui.show.quickSetup.QuickSetupListItem
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_show.episodes.EpisodeListItem
import com.michaldrabik.ui_show.helpers.ActionEvent
import com.michaldrabik.ui_show.helpers.EpisodesManager
import com.michaldrabik.ui_show.seasons.SeasonListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class ShowDetailsViewModel @Inject constructor(
  private val mainCase: ShowDetailsMainCase,
  private val actorsCase: ShowDetailsActorsCase,
  private val ratingsCase: ShowDetailsRatingCase,
  private val seeLaterCase: ShowDetailsSeeLaterCase,
  private val archiveCase: ShowDetailsArchiveCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val episodesCase: ShowDetailsEpisodesCase,
  private val commentsCase: ShowDetailsCommentsCase,
  private val relatedShowsCase: ShowDetailsRelatedShowsCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<ShowDetailsUiModel>() {

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private var areSeasonsLocal = false
  private val seasonItems = mutableListOf<SeasonListItem>()

  fun loadShowDetails(id: IdTrakt, context: Context) {
    viewModelScope.launch {
      val progressJob = launchDelayed(500) {
        uiState = ShowDetailsUiModel(showLoading = true)
      }
      try {
        show = mainCase.loadDetails(id)
        Analytics.logShowDetailsDisplay(show)

        val isSignedIn = userManager.isAuthorized()
        val isFollowed = async { myShowsCase.isMyShows(show) }
        val isWatchLater = async { seeLaterCase.isSeeLater(show) }
        val isArchived = async { archiveCase.isArchived(show) }
        val followedState = FollowedState(
          isMyShows = isFollowed.await(),
          isSeeLater = isWatchLater.await(),
          isArchived = isArchived.await(),
          withAnimation = false
        )

        progressJob.cancel()
        uiState = ShowDetailsUiModel(
          show = show,
          showLoading = false,
          followedState = followedState,
          ratingState = RatingState(rateAllowed = isSignedIn, rateLoading = false)
        )

        launch { loadNextEpisode(show) }
        launch { loadBackgroundImage(show) }
        launch { loadActors(show) }
        launch {
          areSeasonsLoaded = false
          loadSeasons(show, true) //TODO isOnline()
          if (followedState.isMyShows) {
            announcementManager.refreshEpisodesAnnouncements(context)
          }
          areSeasonsLoaded = true
        }
        launch { loadRelatedShows(show) }
        if (isSignedIn) launch { loadRating(show) }
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadShow)
        progressJob.cancel()
      }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = episodesCase.loadNextEpisode(show.ids.trakt)
      uiState = ShowDetailsUiModel(nextEpisode = episode)
    } catch (t: Throwable) {
      // NOOP
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    uiState = try {
      val backgroundImage = imagesProvider.loadRemoteImage(show, FANART)
      ShowDetailsUiModel(image = backgroundImage)
    } catch (t: Throwable) {
      ShowDetailsUiModel(image = Image.createUnavailable(FANART))
    }
  }

  private suspend fun loadActors(show: Show) {
    uiState = try {
      val actors = actorsCase.loadActors(show)
      ShowDetailsUiModel(actors = actors)
    } catch (t: Throwable) {
      ShowDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadSeasons(show: Show, isOnline: Boolean): List<Season> = try {
    val (seasons, isLocal) = episodesCase.loadSeasons(show, isOnline)
    areSeasonsLocal = isLocal
    val seasonsItems = seasons
      .map {
        val episodes = it.episodes.map { episode -> EpisodeListItem(episode, it, false) }
        SeasonListItem(it, episodes, isWatched = false)
      }
      .sortedByDescending { it.season.number }

    val calculated = markWatchedEpisodes(seasonsItems)
    uiState = ShowDetailsUiModel(seasons = calculated)
    seasons
  } catch (t: Throwable) {
    uiState = ShowDetailsUiModel(seasons = emptyList())
    emptyList()
  }

  private suspend fun loadRelatedShows(show: Show) {
    uiState = try {
      val relatedShows = relatedShowsCase.loadRelatedShows(show).map {
        val image = imagesProvider.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      ShowDetailsUiModel(relatedShows = relatedShows)
    } catch (t: Throwable) {
      ShowDetailsUiModel(relatedShows = emptyList())
    }
  }

  fun loadComments() {
    viewModelScope.launch {
      uiState = try {
        val comments = commentsCase.loadComments(show)
        ShowDetailsUiModel(comments = comments)
      } catch (t: Throwable) {
        ShowDetailsUiModel(comments = emptyList())
      }
    }
    Analytics.logShowCommentsClick(show)
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
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  private suspend fun loadRating(show: Show) {
    try {
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
      val rating = ratingsCase.loadRating(show)
      uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
    } catch (error: Throwable) {
      Timber.e(error)
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.addRating(show, rating)
        val userRating = TraktRating(show.ids.trakt, rating)
        uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRated)
        Analytics.logShowRated(show, rating)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun deleteRating() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.deleteRating(show)
        uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = TraktRating.EMPTY, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRatingDeleted)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun addFollowedShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }
      myShowsCase.addToMyShows(show, seasons, episodes)

      uiState = ShowDetailsUiModel(followedState = FollowedState.inMyShows())

      announcementManager.refreshEpisodesAnnouncements(context)
      Analytics.logShowAddToMyShows(show)
    }
  }

  fun addSeeLaterShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      seeLaterCase.addToSeeLater(show)
      quickSyncManager.scheduleShowsSeeLater(context, listOf(show.traktId))

      uiState = ShowDetailsUiModel(followedState = FollowedState.inSeeLater())

      Analytics.logShowAddToSeeLater(show)
    }
  }

  fun addArchiveShow() {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      archiveCase.addToArchive(show, removeLocalData = !areSeasonsLocal)
      uiState = ShowDetailsUiModel(followedState = FollowedState.inArchive())
      Analytics.logShowAddToArchive(show)
    }
  }

  fun removeFromFollowed(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val isMyShows = myShowsCase.isMyShows(show)
      val isSeeLater = seeLaterCase.isSeeLater(show)
      val isArchived = archiveCase.isArchived(show)

      when {
        isMyShows -> {
          myShowsCase.removeFromMyShows(show, removeLocalData = !areSeasonsLocal)
        }
        isSeeLater -> {
          seeLaterCase.removeFromSeeLater(show)
          quickSyncManager.clearShowsSeeLater(listOf(show.traktId))
        }
        isArchived -> {
          archiveCase.removeFromArchive(show)
        }
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal

      val state = FollowedState.notFollowed()
      val event = ActionEvent(showRemoveTrakt)
      uiState = when {
        isMyShows || isArchived -> ShowDetailsUiModel(followedState = state, removeFromTraktHistory = event)
        isSeeLater -> ShowDetailsUiModel(followedState = state, removeFromTraktSeeLater = event)
        else -> error("Unexpected show state")
      }

      announcementManager.refreshEpisodesAnnouncements(context)
    }
  }

  fun removeFromTraktHistory() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showFromTraktLoading = true)
        myShowsCase.removeTraktHistory(show)
        refreshWatchedEpisodes()
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false, removeFromTraktHistory = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }

  fun removeFromTraktSeeLater() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showFromTraktLoading = true)
        seeLaterCase.removeTraktSeeLater(show)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false, removeFromTraktSeeLater = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }

  fun setWatchedEpisode(
    context: Context,
    episode: Episode,
    season: Season,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      when {
        isChecked -> {
          episodesManager.setEpisodeWatched(bundle)
          if (myShowsCase.isMyShows(show) || seeLaterCase.isSeeLater(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(context, listOf(episode.ids.trakt.id))
          }
        }
        else -> episodesManager.setEpisodeUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  fun setWatchedSeason(context: Context, season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = SeasonBundle(season, show)
      when {
        isChecked -> {
          val episodesAdded = episodesManager.setSeasonWatched(bundle)
          if (myShowsCase.isMyShows(show) || seeLaterCase.isSeeLater(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(context, episodesAdded.map { it.ids.trakt.id })
          }
        }
        else -> episodesManager.setSeasonUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  private fun checkSeasonsLoaded(): Boolean {
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
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

  fun setQuickProgress(context: Context, item: QuickSetupListItem?) {
    if (item == null) return
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
      episodesManager.setAllUnwatched(show)
      val seasons = seasonItems.map { it.season }
      seasons
        .filter { it.number < item.season.number }
        .forEach { season ->
          setWatchedSeason(context, season, true)
        }

      val season = seasons.find { it.number == item.season.number }
      season?.episodes
        ?.filter { it.number <= item.episode.number }
        ?.forEach { episode ->
          setWatchedEpisode(context, episode, season, true)
        }

      _messageLiveData.value = MessageEvent.info(R.string.textShowQuickProgressDone)
      Analytics.logShowQuickProgress(show)
    }
  }
}
