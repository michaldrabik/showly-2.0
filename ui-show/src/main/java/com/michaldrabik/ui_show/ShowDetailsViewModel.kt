package com.michaldrabik.ui_show

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError.CoroutineCancellation
import com.michaldrabik.common.errors.ShowlyError.ResourceNotFoundError
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.ShowDetailsEvent.Finish
import com.michaldrabik.ui_show.ShowDetailsEvent.RemoveFromTrakt
import com.michaldrabik.ui_show.ShowDetailsEvent.ShowLoaded
import com.michaldrabik.ui_show.ShowDetailsUiState.FollowedState
import com.michaldrabik.ui_show.cases.ShowDetailsEpisodesCase
import com.michaldrabik.ui_show.cases.ShowDetailsHiddenCase
import com.michaldrabik.ui_show.cases.ShowDetailsListsCase
import com.michaldrabik.ui_show.cases.ShowDetailsMainCase
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsQuickProgressCase
import com.michaldrabik.ui_show.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.cases.ShowDetailsWatchlistCase
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.quick_setup.QuickSetupListItem
import com.michaldrabik.ui_show.seasons.SeasonListItem
import com.michaldrabik.ui_show.sections.ratings.cases.ShowDetailsRatingCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowDetailsViewModel @Inject constructor(
  private val mainCase: ShowDetailsMainCase,
  private val translationCase: ShowDetailsTranslationCase,
  private val ratingsCase: ShowDetailsRatingCase,
  private val watchlistCase: ShowDetailsWatchlistCase,
  private val hiddenCase: ShowDetailsHiddenCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val episodesCase: ShowDetailsEpisodesCase,
  private val listsCase: ShowDetailsListsCase,
  private val quickProgressCase: ShowDetailsQuickProgressCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val _parentEvents = MutableSharedFlow<ShowDetailsEvent<*>>()
  val parentEvents = _parentEvents.asSharedFlow()

  private val showState = MutableStateFlow<Show?>(null)
  private val showLoadingState = MutableStateFlow<Boolean?>(null)
  private val imageState = MutableStateFlow<Image?>(null)
  private val seasonsState = MutableStateFlow<List<SeasonListItem>?>(null)
  private val nextEpisodeState = MutableStateFlow<NextEpisodeBundle?>(null)
  private val followedState = MutableStateFlow<FollowedState?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val signedInState = MutableStateFlow(false)
  private val premiumState = MutableStateFlow(false)
  private val listsCountState = MutableStateFlow(0)

  private var show by notNull<Show>()
  private var areSeasonsLocal = false

  fun loadDetails(id: IdTrakt) {
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        showLoadingState.value = true
      }
      try {
        show = mainCase.loadDetails(id)
        _parentEvents.emit(ShowLoaded(show))
        Analytics.logShowDetailsDisplay(show)

        val isSignedIn = userManager.isAuthorized()
        val isMyShow = async { myShowsCase.isMyShows(show) }
        val isWatchLater = async { watchlistCase.isWatchlist(show) }
        val isArchived = async { hiddenCase.isHidden(show) }
        val isFollowed = FollowedState(
          isMyShows = isMyShow.await(),
          isWatchlist = isWatchLater.await(),
          isHidden = isArchived.await(),
          withAnimation = false
        )

        progressJob.cancel()

        showState.value = show
        showLoadingState.value = false
        followedState.value = isFollowed
        ratingState.value = RatingState(rateAllowed = isSignedIn, rateLoading = false)
        countryState.value = AppCountry.fromCode(settingsRepository.country)
        premiumState.value = settingsRepository.isPremium
        signedInState.value = isSignedIn

        loadBackgroundImage(show)
        loadListsCount(show)
        loadRating()
        launch { loadNextEpisode(show) }
        launch { loadTranslation(show) }
        launch { loadSeasons(show) }
      } catch (error: Throwable) {
        Timber.e(error)
        progressJob.cancel()
        when (ErrorHelper.parse(error)) {
          is CoroutineCancellation -> rethrowCancellation(error)
          is ResourceNotFoundError -> {
            // Malformed Trakt data or duplicate show.
            messageChannel.send(MessageEvent.Info(R.string.errorMalformedShow))
            Logger.record(error, "Source" to "ShowDetailsViewModel")
          }
          else -> {
            messageChannel.send(MessageEvent.Error(R.string.errorCouldNotLoadShow))
            Logger.record(error, "Source" to "ShowDetailsViewModel")
          }
        }
      }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = episodesCase.loadNextEpisode(show.ids.trakt)
      val dateFormat = dateFormatProvider.loadFullHourFormat()
      episode?.let {
        val nextEpisode = NextEpisodeBundle(Pair(show, it), dateFormat = dateFormat)
        nextEpisodeState.value = nextEpisode
        val translation = translationCase.loadTranslation(episode, show)
        if (translation?.title?.isNotBlank() == true) {
          val translated = it.copy(title = translation.title)
          val nextEpisodeTranslated = NextEpisodeBundle(Pair(show, translated), dateFormat = dateFormat)
          nextEpisodeState.value = nextEpisodeTranslated
        }
      }
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "ShowDetailsViewModel::loadNextEpisode()")
      rethrowCancellation(error)
    }
  }

  fun loadBackgroundImage(show: Show? = null) {
    viewModelScope.launch {
      try {
        val backgroundImage = imagesProvider.loadRemoteImage(show ?: this@ShowDetailsViewModel.show, FANART)
        imageState.value = backgroundImage
      } catch (error: Throwable) {
        imageState.value = Image.createUnavailable(FANART)
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  private suspend fun loadSeasons(show: Show) = try {
    val (seasons, isLocal) = episodesCase.loadSeasons(show)
    areSeasonsLocal = isLocal
    val calculated = markWatchedEpisodes(seasons)
    seasonsState.value = calculated
  } catch (error: Throwable) {
    seasonsState.value = emptyList()
  }

  private suspend fun loadTranslation(show: Show) {
    try {
      translationCase.loadTranslation(show)?.let {
        translationState.value = it
      }
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "ShowDetailsViewModel::loadTranslation()")
      rethrowCancellation(error)
    }
  }

  fun loadSeasonTranslation(seasonItem: SeasonListItem) {
    viewModelScope.launch {
      try {
        val translations = translationCase.loadTranslations(seasonItem.season, show)
        if (translations.isEmpty()) return@launch

        val episodes = seasonItem.episodes.toMutableList()
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

        val updatedItem = seasonItem.copy(episodes = episodes, updatedAt = nowUtcMillis())
        val seasonItems = seasonsState.value?.toMutableList() ?: mutableListOf()
        seasonItems.findReplace(updatedItem) { it.id == updatedItem.id }
        val calculated = markWatchedEpisodes(seasonItems)
        seasonsState.value = calculated
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ShowDetailsViewModel::loadSeasonTranslation()")
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  fun loadListsCount(show: Show? = null) {
    viewModelScope.launch {
      val count = listsCase.countLists(show ?: this@ShowDetailsViewModel.show)
      listsCountState.value = count
    }
  }

  fun loadPremium() {
    premiumState.value = settingsRepository.isPremium
  }

  fun loadRating() {
    viewModelScope.launch {
      val isSignedIn = userManager.isAuthorized()
      if (!isSignedIn) return@launch
      try {
        ratingState.value = RatingState(rateLoading = true, rateAllowed = isSignedIn)
        val rating = ratingsCase.loadRating(show)
        ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn, userRating = rating ?: TraktRating.EMPTY)
      } catch (error: Throwable) {
        ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn)
        rethrowCancellation(error)
      }
    }
  }

  fun addFollowedShow() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      val seasonItems = seasonsState.value?.toList() ?: emptyList()
      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }

      myShowsCase.addToMyShows(show, seasons, episodes)
      followedState.value = FollowedState.inMyShows()
      Analytics.logShowAddToMyShows(show)
    }
  }

  fun addWatchlistShow() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      watchlistCase.addToWatchlist(show)
      followedState.value = FollowedState.inWatchlist()
      Analytics.logShowAddToWatchlistShows(show)
    }
  }

  fun addHiddenShow() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      hiddenCase.addToHidden(show, removeLocalData = !areSeasonsLocal)
      followedState.value = FollowedState.inHidden()
      Analytics.logShowAddToArchive(show)
    }
  }

  fun removeFromFollowed() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      val isMyShows = myShowsCase.isMyShows(show)
      val isWatchlist = watchlistCase.isWatchlist(show)
      val isArchived = hiddenCase.isHidden(show)

      when {
        isMyShows -> myShowsCase.removeFromMyShows(show, removeLocalData = !areSeasonsLocal)
        isWatchlist -> watchlistCase.removeFromWatchlist(show)
        isArchived -> hiddenCase.removeFromHidden(show)
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal

      val state = FollowedState.idle()
      val ids = listOf(show.ids.trakt)
      val mode = RemoveTraktBottomSheet.Mode.SHOW
      when {
        isMyShows -> {
          followedState.value = state
          if (showRemoveTrakt) {
            eventChannel.send(RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktProgress, mode, ids))
          }
        }
        isWatchlist -> {
          followedState.value = state
          if (showRemoveTrakt) {
            eventChannel.send(RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktWatchlist, mode, ids))
          }
        }
        isArchived -> {
          followedState.value = state
          if (showRemoveTrakt) {
            eventChannel.send(RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktHidden, mode, ids))
          }
        }
        else -> error("Unexpected show state.")
      }

      announcementManager.refreshShowsAnnouncements()
    }
  }

  fun removeMalformedShow(id: IdTrakt) {
    viewModelScope.launch {
      try {
        mainCase.removeMalformedShow(id)
      } catch (error: Throwable) {
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        eventChannel.send(Finish)
      }
    }
  }

  fun setEpisodeWatched(
    episode: Episode,
    season: Season,
    isChecked: Boolean,
    removeTrakt: Boolean = false,
    clearProgress: Boolean = false
  ) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      val isMyShows = myShowsCase.isMyShows(show)
      val isCollection = isMyShows || watchlistCase.isWatchlist(show) || hiddenCase.isHidden(show)
      when {
        isChecked -> {
          episodesManager.setEpisodeWatched(bundle)
          if (isMyShows) {
            quickSyncManager.scheduleEpisodes(
              episodesIds = listOf(episode.ids.trakt.id),
              showId = show.traktId,
              clearProgress = clearProgress
            )
          }
        }
        else -> {
          episodesManager.setEpisodeUnwatched(bundle)
          quickSyncManager.clearEpisodes(listOf(episode.ids.trakt.id))

          val traktQuickRemoveEnabled = removeTrakt && settingsRepository.load().traktQuickRemoveEnabled
          val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal && isCollection
          if (showRemoveTrakt) {
            val ids = listOf(episode.ids.trakt)
            val mode = RemoveTraktBottomSheet.Mode.EPISODE
            eventChannel.send(RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktProgress, mode, ids))
          }
        }
      }
      refreshWatchedEpisodes()
    }
  }

  fun setSeasonWatched(
    season: Season,
    isChecked: Boolean,
    removeTrakt: Boolean = false
  ) {
    viewModelScope.launch {
      val bundle = SeasonBundle(season, show)
      val isMyShows = myShowsCase.isMyShows(show)
      val isCollection = isMyShows || watchlistCase.isWatchlist(show) || hiddenCase.isHidden(show)
      when {
        isChecked -> {
          val episodesAdded = episodesManager.setSeasonWatched(bundle)
          if (isMyShows) {
            quickSyncManager.scheduleEpisodes(episodesAdded.map { it.ids.trakt.id })
          }
        }
        else -> {
          episodesManager.setSeasonUnwatched(bundle)
          quickSyncManager.clearEpisodes(season.episodes.map { it.ids.trakt.id })

          val traktQuickRemoveEnabled = removeTrakt && settingsRepository.load().traktQuickRemoveEnabled
          val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal && isCollection
          if (showRemoveTrakt) {
            val ids = season.episodes.map { it.ids.trakt }
            val mode = RemoveTraktBottomSheet.Mode.EPISODE
            eventChannel.send(RemoveFromTrakt(R.id.actionShowDetailsFragmentToRemoveTraktProgress, mode, ids))
          }
        }
      }
      refreshWatchedEpisodes()
    }
  }

  fun setQuickProgress(item: QuickSetupListItem?) {
    viewModelScope.launch {
      if (item == null || !checkSeasonsLoaded()) return@launch

      val seasonItems = seasonsState.value?.toList() ?: emptyList()
      quickProgressCase.setQuickProgress(item, seasonItems, show)

      messageChannel.send(MessageEvent.Info(R.string.textShowQuickProgressDone))
      refreshWatchedEpisodes()
      Analytics.logShowQuickProgress(show)
    }
  }

  fun refreshEpisodesRatings() {
    viewModelScope.launch {
      val seasonItems = seasonsState.value?.toList() ?: emptyList()
      val items = seasonItems.map { seasonItem ->
        val ratingSeason = ratingsCase.loadRating(seasonItem.season)
        val episodes = seasonItem.episodes.map { episodeItem ->
          async {
            val ratingEpisode = ratingsCase.loadRating(episodeItem.episode)
            episodeItem.copy(myRating = ratingEpisode)
          }
        }.awaitAll()
        seasonItem.copy(episodes = episodes, userRating = seasonItem.userRating.copy(ratingSeason))
      }
      seasonsState.value = items
    }
  }

  fun launchRefreshWatchedEpisodes() {
    viewModelScope.launch {
      refreshWatchedEpisodes()
    }
  }

  private suspend fun checkSeasonsLoaded(): Boolean {
    if (seasonsState.value == null) {
      messageChannel.send(MessageEvent.Info(R.string.errorSeasonsNotLoaded))
      return false
    }
    return true
  }

  private suspend fun refreshWatchedEpisodes() {
    val seasonItems = seasonsState.value?.toList() ?: emptyList()
    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
    seasonsState.value = updatedSeasonItems
  }

  private suspend fun markWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> =
    coroutineScope {
      val items = mutableListOf<SeasonListItem>()

      val (watchedSeasonsIds, watchedEpisodesIds) = awaitAll(
        async { episodesManager.getWatchedSeasonsIds(show) },
        async { episodesManager.getWatchedEpisodesIds(show) }
      )

      seasonsList.forEach { item ->
        val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
        val episodes = item.episodes.map { episodeItem ->
          val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
          episodeItem.copy(season = item.season, isWatched = isEpisodeWatched)
        }
        val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
        items.add(updated)
      }

      items
    }

  fun refreshAnnouncements() {
    viewModelScope.launch {
      val isFollowed = myShowsCase.isMyShows(show)
      if (isFollowed) {
        announcementManager.refreshShowsAnnouncements()
      }
    }
  }

  fun onPersonDetails(person: Person) {
    viewModelScope.launch {
      _parentEvents.emit(ShowDetailsEvent.SaveOpenedPerson(person))
    }
  }

  val uiState = combine(
    showState,
    showLoadingState,
    imageState,
    seasonsState,
    nextEpisodeState,
    followedState,
    ratingState,
    translationState,
    countryState,
    signedInState,
    premiumState,
    listsCountState
  ) { s1, s2, s4, s5, s8, s9, s10, s11, s12, s13, s14, s15 ->
    ShowDetailsUiState(
      show = s1,
      showLoading = s2,
      image = s4,
      seasons = s5,
      nextEpisode = s8,
      followedState = s9,
      ratingState = s10,
      translation = s11,
      country = s12,
      isSignedIn = s13,
      isPremium = s14,
      listsCount = s15
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsUiState()
  )
}
