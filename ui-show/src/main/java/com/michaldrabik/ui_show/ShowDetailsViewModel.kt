package com.michaldrabik.ui_show

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError.CoroutineCancellation
import com.michaldrabik.common.errors.ShowlyError.ResourceNotFoundError
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.ShowDetailsEvent.Finish
import com.michaldrabik.ui_show.ShowDetailsEvent.RemoveFromTrakt
import com.michaldrabik.ui_show.ShowDetailsUiState.FollowedState
import com.michaldrabik.ui_show.cases.ShowDetailsHiddenCase
import com.michaldrabik.ui_show.cases.ShowDetailsListsCase
import com.michaldrabik.ui_show.cases.ShowDetailsMainCase
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.cases.ShowDetailsWatchlistCase
import com.michaldrabik.ui_show.helpers.ShowDetailsMeta
import com.michaldrabik.ui_show.sections.ratings.cases.ShowDetailsRatingCase
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowDetailsViewModel @Inject constructor(
  private val mainCase: ShowDetailsMainCase,
  private val translationCase: ShowDetailsTranslationCase,
  private val ratingsCase: ShowDetailsRatingCase,
  private val watchlistCase: ShowDetailsWatchlistCase,
  private val hiddenCase: ShowDetailsHiddenCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val listsCase: ShowDetailsListsCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val seasonsCache: SeasonsCache,
  private val imagesProvider: ShowImagesProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var show: Show

  private val _parentEvents = MutableSharedFlow<ShowDetailsEvent<*>>(extraBufferCapacity = 1)
  val parentEvents = _parentEvents.asSharedFlow()

  private val showState = MutableStateFlow<Show?>(null)
  val parentShowState = showState.asStateFlow()
  private val showLoadingState = MutableStateFlow<Boolean?>(null)
  private val imageState = MutableStateFlow<Image?>(null)
  private val followedState = MutableStateFlow<FollowedState?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val listsCountState = MutableStateFlow(0)
  private val metaState = MutableStateFlow<ShowDetailsMeta?>(null)

  fun loadDetails(id: IdTrakt) {
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        showLoadingState.value = true
      }
      try {
        show = mainCase.loadDetails(id)
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
        metaState.value = ShowDetailsMeta(
          isSignedIn = isSignedIn,
          isPremium = settingsRepository.isPremium
        )

        loadBackgroundImage(show)
        loadListsCount(show)
        loadUserRating()
        launch { loadTranslation(show) }
      } catch (error: Throwable) {
        Timber.e(error)
        progressJob.cancel()
        when (ErrorHelper.parse(error)) {
          is CoroutineCancellation -> rethrowCancellation(error)
          is ResourceNotFoundError -> {
            // Malformed Trakt data or duplicate show.
            messageChannel.send(MessageEvent.Info(R.string.errorMalformedShow))
            Logger.record(error, "ShowDetailsViewModel::loadDetails(${id.id})")
          }
          else -> {
            messageChannel.send(MessageEvent.Error(R.string.errorCouldNotLoadShow))
            Logger.record(error, "ShowDetailsViewModel::loadDetails(${id.id})")
          }
        }
      }
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

  private suspend fun loadTranslation(show: Show) {
    try {
      translationCase.loadTranslation(show)?.let {
        translationState.value = it
      }
    } catch (error: Throwable) {
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  fun loadListsCount(show: Show? = null) {
    viewModelScope.launch {
      val count = listsCase.countLists(show ?: this@ShowDetailsViewModel.show)
      listsCountState.value = count
    }
  }

  fun loadPremium() {
    metaState.update { it?.copy(isPremium = settingsRepository.isPremium) }
  }

  fun loadUserRating() {
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

      val seasonItems = seasonsCache.loadSeasons(show.ids.trakt) ?: emptyList()
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

      val areSeasonsLocal = seasonsCache.areSeasonsLocal(show.ids.trakt)
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
      val areSeasonsLocal = seasonsCache.areSeasonsLocal(show.ids.trakt)

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

  fun refreshSeasons() {
    viewModelScope.launch {
      _parentEvents.emit(ShowDetailsEvent.RefreshSeasons)
    }
  }

  private suspend fun checkSeasonsLoaded(): Boolean {
    if (!seasonsCache.hasSeasons(show.ids.trakt)) {
      messageChannel.send(MessageEvent.Info(R.string.errorSeasonsNotLoaded))
      return false
    }
    return true
  }

  fun onPersonDetails(person: Person) {
    viewModelScope.launch {
      _parentEvents.emit(ShowDetailsEvent.SaveOpenedPerson(person))
    }
  }

  override fun onCleared() {
    if (this::show.isInitialized) {
      seasonsCache.clear(show.ids.trakt)
    }
    super.onCleared()
  }

  val uiState = combine(
    showState,
    showLoadingState,
    imageState,
    followedState,
    ratingState,
    translationState,
    listsCountState,
    metaState
  ) { s1, s2, s4, s9, s10, s11, s15, s16 ->
    ShowDetailsUiState(
      show = s1,
      showLoading = s2,
      image = s4,
      followedState = s9,
      ratingState = s10,
      translation = s11,
      listsCount = s15,
      meta = s16
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsUiState()
  )
}
