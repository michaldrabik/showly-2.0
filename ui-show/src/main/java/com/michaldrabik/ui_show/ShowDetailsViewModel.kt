package com.michaldrabik.ui_show

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.episodes.EpisodesManager
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_show.ShowDetailsUiState.FollowedState2
import com.michaldrabik.ui_show.cases.ShowDetailsActorsCase
import com.michaldrabik.ui_show.cases.ShowDetailsArchiveCase
import com.michaldrabik.ui_show.cases.ShowDetailsCommentsCase
import com.michaldrabik.ui_show.cases.ShowDetailsEpisodesCase
import com.michaldrabik.ui_show.cases.ShowDetailsListsCase
import com.michaldrabik.ui_show.cases.ShowDetailsMainCase
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsRatingCase
import com.michaldrabik.ui_show.cases.ShowDetailsRelatedShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsStreamingCase
import com.michaldrabik.ui_show.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.cases.ShowDetailsWatchlistCase
import com.michaldrabik.ui_show.episodes.EpisodeListItem
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.helpers.StreamingsBundle
import com.michaldrabik.ui_show.quickSetup.QuickSetupListItem
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.seasons.SeasonListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowDetailsViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val mainCase: ShowDetailsMainCase,
  private val actorsCase: ShowDetailsActorsCase,
  private val translationCase: ShowDetailsTranslationCase,
  private val ratingsCase: ShowDetailsRatingCase,
  private val watchlistCase: ShowDetailsWatchlistCase,
  private val archiveCase: ShowDetailsArchiveCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val episodesCase: ShowDetailsEpisodesCase,
  private val commentsCase: ShowDetailsCommentsCase,
  private val listsCase: ShowDetailsListsCase,
  private val streamingsCase: ShowDetailsStreamingCase,
  private val relatedShowsCase: ShowDetailsRelatedShowsCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) : BaseViewModel() {

  private val showState = MutableStateFlow<Show?>(null)
  private val showLoadingState = MutableStateFlow<Boolean?>(null)
  private val showRatingsState = MutableStateFlow<Ratings?>(null)
  private val imageState = MutableStateFlow<Image?>(null)
  private val actorsState = MutableStateFlow<List<Actor>?>(null)
  private val seasonsState = MutableStateFlow<List<SeasonListItem>?>(null)
  private val relatedState = MutableStateFlow<List<RelatedListItem>?>(null)
  private val nextEpisodeState = MutableStateFlow<NextEpisodeBundle?>(null)
  private val commentsState = MutableStateFlow<List<Comment>?>(null)
  private val commentsDateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val followedState = MutableStateFlow<FollowedState2?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val streamingsState = MutableStateFlow<StreamingsBundle?>(null)
  private val traktLoadingState = MutableStateFlow(false)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val signedInState = MutableStateFlow(false)
  private val premiumState = MutableStateFlow(false)
  private val listsCountState = MutableStateFlow(0)

  private val seasonTranslationEvent = MutableStateFlow<ActionEvent<SeasonListItem>?>(null)
  private val removeTraktHistoryEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val removeTraktWatchlistEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val finishedEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private var areSeasonsLocal = false
  private val seasonItems = mutableListOf<SeasonListItem>()

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
        val isArchived = async { archiveCase.isArchived(show) }
        val isFollowed = FollowedState2(
          isMyShows = isMyShow.await(),
          isWatchlist = isWatchLater.await(),
          isArchived = isArchived.await(),
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
        commentsDateFormatState.value = dateFormatProvider.loadFullHourFormat()

        loadBackgroundImage(show)
        loadListsCount(show)
        launch { loadRating(show, isSignedIn) }
        launch { loadRatings(show) }
        launch { loadStreamings(show) }
        launch { loadActors(show) }
        launch { loadNextEpisode(show) }
        launch { loadTranslation(show) }
        launch { loadRelatedShows(show) }
        launch {
          areSeasonsLoaded = false
          loadSeasons(show, (context as OnlineStatusProvider).isOnline())
          areSeasonsLoaded = true
        }
      } catch (error: Throwable) {
        progressJob.cancel()
        if (error is HttpException && error.code() == 404) {
          // Malformed Trakt data or duplicate show.
          _messageState.emit(MessageEvent.info(R.string.errorMalformedShow))
        } else {
          _messageState.emit(MessageEvent.error(R.string.errorCouldNotLoadShow))
        }
        Logger.record(error, "Source" to "ShowDetailsViewModel")
        Timber.e(error)
        rethrowCancellation(error)
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

  private suspend fun loadActors(show: Show) {
    try {
      val actors = actorsCase.loadActors(show)
      actorsState.value = actors
    } catch (error: Throwable) {
      actorsState.value = emptyList()
      rethrowCancellation(error)
    }
  }

  private suspend fun loadSeasons(show: Show, isOnline: Boolean): List<Season> = try {
    val (seasons, isLocal) = episodesCase.loadSeasons(show, isOnline)
    areSeasonsLocal = isLocal
    val format = dateFormatProvider.loadFullHourFormat()
    val seasonsItems = seasons
      .map {
        val episodes = it.episodes.map { episode ->
          val rating = ratingsCase.loadRating(episode)
          val translation = translationCase.loadTranslation(episode, show, onlyLocal = true)
          EpisodeListItem(episode, it, false, translation, rating, format)
        }
        SeasonListItem(show, it, episodes, isWatched = false)
      }
      .sortedByDescending { it.season.number }

    val calculated = markWatchedEpisodes(seasonsItems)
    seasonsState.value = calculated
    seasons
  } catch (error: Throwable) {
    seasonsState.value = emptyList()
    emptyList()
  }

  private suspend fun loadRelatedShows(show: Show) {
    try {
      val relatedShows = relatedShowsCase.loadRelatedShows(show).map {
        val image = imagesProvider.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      relatedState.value = relatedShows
    } catch (error: Throwable) {
      relatedState.value = emptyList()
      rethrowCancellation(error)
    }
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

  private suspend fun loadStreamings(show: Show) {
    try {
      val localStreamings = streamingsCase.getLocalStreamingServices(show)
      streamingsState.value = StreamingsBundle(localStreamings, isLocal = true)

      val remoteStreamings = streamingsCase.loadStreamingServices(show)
      streamingsState.value = StreamingsBundle(remoteStreamings, isLocal = false)
    } catch (error: Throwable) {
      streamingsState.value = StreamingsBundle(emptyList(), isLocal = false)
      rethrowCancellation(error)
    }
  }

  private suspend fun loadRatings(show: Show) {
    val traktRatings = Ratings(
      trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", show.rating), false),
      imdb = Ratings.Value(null, true),
      metascore = Ratings.Value(null, true),
      rottenTomatoes = Ratings.Value(null, true)
    )
    try {
      showRatingsState.value = traktRatings
      val ratings = ratingsCase.loadExternalRatings(show)
      showRatingsState.value = ratings
    } catch (error: Throwable) {
      showRatingsState.value = traktRatings
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
            if (translation.overview.isNotBlank()) {
              val t = Translation(translation.title, translation.overview, translation.language)
              val withTranslation = ep.copy(translation = t)
              episodes.findReplace(withTranslation) { it.id == withTranslation.id }
            }
          }
        }

        val updatedItem = seasonItem.copy(episodes = episodes)
        seasonItems.findReplace(updatedItem) { it.id == updatedItem.id }
        seasonTranslationEvent.value = ActionEvent(updatedItem)
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

  fun loadComments() {
    viewModelScope.launch {
      try {
        val comments = commentsCase.loadComments(show)
        commentsState.value = comments
      } catch (error: Throwable) {
        commentsState.value = emptyList()
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
    Analytics.logShowCommentsClick(show)
  }

  fun loadCommentReplies(comment: Comment) {
    var currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    if (currentComments.any { it.parentId == comment.id }) return

    viewModelScope.launch {
      try {
        val parent = currentComments.find { it.id == comment.id }
        parent?.let { p ->
          val copy = p.copy(isLoading = true)
          currentComments.findReplace(copy) { it.id == p.id }
          commentsState.value = currentComments
        }

        val replies = commentsCase.loadReplies(comment)

        currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
        val parentIndex = currentComments.indexOfFirst { it.id == comment.id }
        if (parentIndex > -1) currentComments.addAll(parentIndex + 1, replies)
        parent?.let {
          currentComments.findReplace(parent.copy(isLoading = false, hasRepliesLoaded = true)) { it.id == comment.id }
        }

        commentsState.value = currentComments
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
        commentsState.value = currentComments
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  fun addNewComment(comment: Comment) {
    val currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    if (!comment.isReply()) {
      currentComments.add(0, comment)
    } else {
      val parentIndex = currentComments.indexOfLast { it.id == comment.parentId }
      if (parentIndex > -1) {
        val parent = currentComments[parentIndex]
        currentComments.add(parentIndex + 1, comment)
        val repliesCount = currentComments.count { it.parentId == parent.id }.toLong()
        currentComments.findReplace(parent.copy(replies = repliesCount)) { it.id == comment.parentId }
      }
    }
    commentsState.value = currentComments
  }

  fun deleteComment(comment: Comment) {
    var currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    val target = currentComments.find { it.id == comment.id } ?: return

    viewModelScope.launch {
      try {
        val copy = target.copy(isLoading = true)
        currentComments.findReplace(copy) { it.id == target.id }
        commentsState.value = currentComments

        commentsCase.delete(target)

        currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
        val targetIndex = currentComments.indexOfFirst { it.id == target.id }
        if (targetIndex > -1) {
          currentComments.removeAt(targetIndex)
          if (target.isReply()) {
            val parent = currentComments.first { it.id == target.parentId }
            val repliesCount = currentComments.count { it.parentId == parent.id }.toLong()
            currentComments.findReplace(parent.copy(replies = repliesCount)) { it.id == target.parentId }
          }
        }

        commentsState.value = currentComments
        _messageState.emit(MessageEvent.info(R.string.textCommentDeleted))
      } catch (t: Throwable) {
        if (t is HttpException && t.code() == 409) {
          _messageState.emit(MessageEvent.error(R.string.errorCommentDelete))
        } else {
          _messageState.emit(MessageEvent.error(R.string.errorGeneral))
        }
        commentsState.value = currentComments
      }
    }
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState.value.relatedShows?.toMutableList() ?: mutableListOf()
      currentItems.findReplace(new) { it.isSameAs(new) }
      relatedState.value = currentItems
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (error: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
        rethrowCancellation(error)
      }
    }
  }

  fun loadPremium() {
    premiumState.value = settingsRepository.isPremium
  }

  private suspend fun loadRating(show: Show, isSignedIn: Boolean) {
    if (!isSignedIn) return
    try {
      ratingState.value = RatingState(rateLoading = true, rateAllowed = isSignedIn)
      val rating = ratingsCase.loadRating(show)
      ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn, userRating = rating ?: TraktRating.EMPTY)
    } catch (error: Throwable) {
      ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn)
      rethrowCancellation(error)
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        ratingState.value = RatingState(rateLoading = true, rateAllowed = true)
        ratingsCase.addRating(show, rating)
        val userRating = TraktRating(show.ids.trakt, rating)
        ratingState.value = RatingState(rateLoading = false, rateAllowed = true, userRating = userRating)
        _messageState.emit(MessageEvent.info(R.string.textRateSaved))
        Analytics.logShowRated(show, rating)
      } catch (error: Throwable) {
        ratingState.value = RatingState(rateLoading = false, rateAllowed = true)
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  fun deleteRating() {
    viewModelScope.launch {
      try {
        ratingState.value = RatingState(rateLoading = true, rateAllowed = true)
        ratingsCase.deleteRating(show)
        ratingState.value = RatingState(rateLoading = false, rateAllowed = true, userRating = TraktRating.EMPTY)
        _messageState.emit(MessageEvent.info(R.string.textShowRatingDeleted))
      } catch (error: Throwable) {
        ratingState.value = RatingState(rateLoading = false, rateAllowed = true)
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  fun addFollowedShow() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }

      myShowsCase.addToMyShows(show, seasons, episodes)
      followedState.value = FollowedState2.inMyShows()
      announcementManager.refreshShowsAnnouncements()
      Analytics.logShowAddToMyShows(show)
    }
  }

  fun addWatchlistShow(context: Context) {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      watchlistCase.addToWatchlist(show)
      quickSyncManager.scheduleShowsWatchlist(listOf(show.traktId))
      followedState.value = FollowedState2.inWatchlist()
      Analytics.logShowAddToWatchlistShows(show)
    }
  }

  fun addArchiveShow() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      archiveCase.addToArchive(show, removeLocalData = !areSeasonsLocal)
      followedState.value = FollowedState2.inArchive()
      Analytics.logShowAddToArchive(show)
    }
  }

  fun removeFromFollowed() {
    viewModelScope.launch {
      if (!checkSeasonsLoaded()) return@launch

      val isMyShows = myShowsCase.isMyShows(show)
      val isWatchlist = watchlistCase.isWatchlist(show)
      val isArchived = archiveCase.isArchived(show)

      when {
        isMyShows -> {
          myShowsCase.removeFromMyShows(show, removeLocalData = !areSeasonsLocal)
        }
        isWatchlist -> {
          watchlistCase.removeFromWatchlist(show)
          quickSyncManager.clearShowsWatchlist(listOf(show.traktId))
        }
        isArchived -> {
          archiveCase.removeFromArchive(show)
        }
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal

      val state = FollowedState2.notFollowed()
      val event = ActionEvent(showRemoveTrakt)
      when {
        isMyShows || isArchived -> {
          followedState.value = state
          removeTraktHistoryEvent.value = event
        }
        isWatchlist -> {
          followedState.value = state
          removeTraktWatchlistEvent.value = event
        }
        else -> error("Unexpected show state.")
      }

      announcementManager.refreshShowsAnnouncements()
    }
  }

  fun removeFromTraktHistory() {
    viewModelScope.launch {
      try {
        traktLoadingState.value = true
        myShowsCase.removeTraktHistory(show)
        refreshWatchedEpisodes()
        _messageState.emit(MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt))
        traktLoadingState.value = false
        removeTraktHistoryEvent.value = ActionEvent(false)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorTraktSyncGeneral))
        traktLoadingState.value = false
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  fun removeFromTraktWatchlist() {
    viewModelScope.launch {
      try {
        traktLoadingState.value = true
        watchlistCase.removeTraktWatchlist(show)
        _messageState.emit(MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt))
        traktLoadingState.value = false
        removeTraktWatchlistEvent.value = ActionEvent(false)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorTraktSyncGeneral))
        traktLoadingState.value = false
        Timber.e(error)
        rethrowCancellation(error)
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
        finishedEvent.value = ActionEvent(true)
      }
    }
  }

  fun setWatchedEpisode(
    context: Context,
    episode: Episode,
    season: Season,
    isChecked: Boolean,
  ) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      when {
        isChecked -> {
          episodesManager.setEpisodeWatched(bundle)
          if (myShowsCase.isMyShows(show) || watchlistCase.isWatchlist(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(listOf(episode.ids.trakt.id))
          }
        }
        else -> {
          episodesManager.setEpisodeUnwatched(bundle)
          quickSyncManager.clearEpisodes(listOf(episode.ids.trakt.id))
        }
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
          if (myShowsCase.isMyShows(show) || watchlistCase.isWatchlist(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(episodesAdded.map { it.ids.trakt.id })
          }
        }
        else -> {
          episodesManager.setSeasonUnwatched(bundle)
          quickSyncManager.clearEpisodes(season.episodes.map { it.ids.trakt.id })
        }
      }
      refreshWatchedEpisodes()
    }
  }

  private suspend fun checkSeasonsLoaded(): Boolean {
    if (!areSeasonsLoaded) {
      _messageState.emit(MessageEvent.info(R.string.errorSeasonsNotLoaded))
      return false
    }
    return true
  }

  fun refreshEpisodesRatings() {
    viewModelScope.launch {
      val items = seasonItems.map { seasonItem ->
        val episodes = seasonItem.episodes.map { episodeItem ->
          val rating = ratingsCase.loadRating(episodeItem.episode)
          episodeItem.copy(myRating = rating)
        }
        seasonItem.copy(episodes = episodes)
      }
      seasonItems.replace(items)
      seasonsState.value = items
    }
  }

  private suspend fun refreshWatchedEpisodes() {
    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
    seasonsState.value = updatedSeasonItems
  }

  private suspend fun markWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> {
    val items = mutableListOf<SeasonListItem>()

    val watchedSeasonsIds = episodesManager.getWatchedSeasonsIds(show)
    val watchedEpisodesIds = episodesManager.getWatchedEpisodesIds(show)

    seasonsList.forEach { item ->
      val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
      val episodes = item.episodes.map { episodeItem ->
        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
        episodeItem.copy(season = item.season, isWatched = isEpisodeWatched)
      }
      val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
      items.add(updated)
    }

    seasonItems.replace(items)
    return items
  }

  fun refreshAnnouncements() {
    viewModelScope.launch {
      val isFollowed = myShowsCase.isMyShows(show)
      if (isFollowed) {
        announcementManager.refreshShowsAnnouncements()
      }
    }
  }

  fun setQuickProgress(context: Context, item: QuickSetupListItem?) {
    viewModelScope.launch {
      if (item == null || !checkSeasonsLoaded()) return@launch

      episodesManager.setAllUnwatched(show, skipSpecials = true)
      val seasons = seasonItems.map { it.season }
      seasons
        .filter { !it.isSpecial() && it.number < item.season.number }
        .forEach { season ->
          setWatchedSeason(context, season, true)
        }

      val season = seasons.find { it.number == item.season.number }
      season?.episodes
        ?.filter { it.number <= item.episode.number }
        ?.forEach { episode ->
          setWatchedEpisode(context, episode, season, true)
        }

      _messageState.emit(MessageEvent.info(R.string.textShowQuickProgressDone))
      Analytics.logShowQuickProgress(show)
    }
  }

  val uiState = combine(
    showState,
    showLoadingState,
    showRatingsState,
    imageState,
    seasonsState,
    actorsState,
    relatedState,
    nextEpisodeState,
    commentsState,
    commentsDateFormatState,
    followedState,
    ratingState,
    streamingsState,
    traktLoadingState,
    translationState,
    countryState,
    signedInState,
    premiumState,
    listsCountState,
    seasonTranslationEvent,
    removeTraktHistoryEvent,
    removeTraktWatchlistEvent,
    finishedEvent
  ) { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23 ->
    ShowDetailsUiState(
      show = s1,
      showLoading = s2,
      ratings = s3,
      image = s4,
      seasons = s5,
      actors = s6,
      relatedShows = s7,
      nextEpisode = s8,
      comments = s9,
      commentsDateFormat = s10,
      followedState = s11,
      ratingState = s12,
      streamings = s13,
      showFromTraktLoading = s14,
      translation = s15,
      country = s16,
      isSignedIn = s17,
      isPremium = s18,
      listsCount = s19,
      seasonTranslation = s20,
      removeFromTraktHistory = s21,
      removeFromTraktWatchlist = s22,
      isFinished = s23
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsUiState()
  )
}
