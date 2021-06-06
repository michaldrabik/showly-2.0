package com.michaldrabik.ui_show

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class ShowDetailsViewModel @Inject constructor(
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
) : BaseViewModel<ShowDetailsUiModel>() {

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private var areSeasonsLocal = false
  private val seasonItems = mutableListOf<SeasonListItem>()

  private val _seasonsLiveData = MutableLiveData<List<SeasonListItem>>()
  private val _actorsLiveData = MutableLiveData<List<Actor>>()
  private val _relatedLiveData = MutableLiveData<List<RelatedListItem>>()
  private val _nextEpisodeLiveData = MutableLiveData<NextEpisodeBundle>()
  private val _streamingsBundle = MutableLiveData<StreamingsBundle>()

  val seasonsLiveData: LiveData<List<SeasonListItem>> get() = _seasonsLiveData
  val actorsLiveData: LiveData<List<Actor>> get() = _actorsLiveData
  val relatedLiveData: LiveData<List<RelatedListItem>> get() = _relatedLiveData
  val nextEpisodeLiveData: LiveData<NextEpisodeBundle> get() = _nextEpisodeLiveData
  val streamingsLiveData: LiveData<StreamingsBundle> get() = _streamingsBundle

  fun loadShowDetails(id: IdTrakt, context: Context) {
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        uiState = ShowDetailsUiModel(showLoading = true)
      }
      try {
        show = mainCase.loadDetails(id)
        Analytics.logShowDetailsDisplay(show)

        val isSignedIn = userManager.isAuthorized()
        val isFollowed = async { myShowsCase.isMyShows(show) }
        val isWatchLater = async { watchlistCase.isWatchlist(show) }
        val isArchived = async { archiveCase.isArchived(show) }
        val followedState = FollowedState(
          isMyShows = isFollowed.await(),
          isWatchlist = isWatchLater.await(),
          isArchived = isArchived.await(),
          withAnimation = false
        )

        progressJob.cancel()
        uiState = ShowDetailsUiModel(
          show = show,
          showLoading = false,
          followedState = followedState,
          ratingState = RatingState(rateAllowed = isSignedIn, rateLoading = false),
          country = AppCountry.fromCode(settingsRepository.country),
          isPremium = settingsRepository.isPremium,
          isSignedIn = isSignedIn,
          commentsDateFormat = dateFormatProvider.loadFullHourFormat()
        )

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
          if (followedState.isMyShows) {
            announcementManager.refreshShowsAnnouncements(context)
          }
          areSeasonsLoaded = true
        }
      } catch (error: Throwable) {
        progressJob.cancel()
        _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadShow)
        Logger.record(error, "Source" to "ShowDetailsViewModel")
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
        _nextEpisodeLiveData.postValue(nextEpisode)
        val translation = translationCase.loadTranslation(episode, show)
        if (translation?.title?.isNotBlank() == true) {
          val translated = it.copy(title = translation.title)
          val nextEpisodeTranslated = NextEpisodeBundle(Pair(show, translated), dateFormat = dateFormat)
          _nextEpisodeLiveData.postValue(nextEpisodeTranslated)
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
        uiState = ShowDetailsUiModel(image = backgroundImage)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(image = Image.createUnavailable(FANART))
        rethrowCancellation(error)
      }
    }
  }

  private suspend fun loadActors(show: Show) {
    try {
      val actors = actorsCase.loadActors(show)
      _actorsLiveData.postValue(actors)
    } catch (error: Throwable) {
      _actorsLiveData.postValue(emptyList())
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
    _seasonsLiveData.postValue(calculated)
    seasons
  } catch (error: Throwable) {
    _seasonsLiveData.postValue(emptyList())
    emptyList()
  }

  private suspend fun loadRelatedShows(show: Show) {
    try {
      val relatedShows = relatedShowsCase.loadRelatedShows(show).map {
        val image = imagesProvider.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      _relatedLiveData.postValue(relatedShows)
    } catch (error: Throwable) {
      _relatedLiveData.postValue(emptyList())
      rethrowCancellation(error)
    }
  }

  private suspend fun loadTranslation(show: Show) {
    try {
      val translation = translationCase.loadTranslation(show)
      translation?.let {
        uiState = ShowDetailsUiModel(translation = it)
      }
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "ShowDetailsViewModel::loadTranslation()")
      rethrowCancellation(error)
    }
  }

  private suspend fun loadStreamings(show: Show) {
    try {
      val localStreamings = streamingsCase.getLocalStreamingServices(show)
      _streamingsBundle.postValue(StreamingsBundle(localStreamings, isLocal = true))

      val remoteStreamings = streamingsCase.loadStreamingServices(show)
      _streamingsBundle.postValue(StreamingsBundle(remoteStreamings, isLocal = false))
    } catch (error: Throwable) {
      _streamingsBundle.postValue(StreamingsBundle(emptyList(), isLocal = false))
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
      uiState = ShowDetailsUiModel(ratings = traktRatings)
      val ratings = ratingsCase.loadExternalRatings(show)
      uiState = ShowDetailsUiModel(ratings = ratings)
    } catch (error: Throwable) {
      uiState = ShowDetailsUiModel(ratings = traktRatings)
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
        uiState = ShowDetailsUiModel(seasonTranslation = ActionEvent(updatedItem))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ShowDetailsViewModel::loadSeasonTranslation()")
        rethrowCancellation(error)
      }
    }
  }

  fun loadListsCount(show: Show? = null) {
    viewModelScope.launch {
      val count = listsCase.countLists(show ?: this@ShowDetailsViewModel.show)
      uiState = ShowDetailsUiModel(listsCount = count)
    }
  }

  fun loadComments() {
    viewModelScope.launch {
      try {
        val comments = commentsCase.loadComments(show)
        uiState = ShowDetailsUiModel(comments = comments)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(comments = emptyList())
        rethrowCancellation(error)
      }
    }
    Analytics.logShowCommentsClick(show)
  }

  fun loadCommentReplies(comment: Comment) {
    var currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
    if (currentComments.any { it.parentId == comment.id }) return

    viewModelScope.launch {
      try {
        val parent = currentComments.find { it.id == comment.id }
        parent?.let { p ->
          val copy = p.copy(isLoading = true)
          currentComments.findReplace(copy) { it.id == p.id }
          uiState = ShowDetailsUiModel(comments = currentComments)
        }

        val replies = commentsCase.loadReplies(comment)

        currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
        val parentIndex = currentComments.indexOfFirst { it.id == comment.id }
        if (parentIndex > -1) currentComments.addAll(parentIndex + 1, replies)
        parent?.let {
          currentComments.findReplace(parent.copy(isLoading = false, hasRepliesLoaded = true)) { it.id == comment.id }
        }

        uiState = ShowDetailsUiModel(comments = currentComments)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        uiState = ShowDetailsUiModel(comments = currentComments)
        rethrowCancellation(error)
      }
    }
  }

  fun addNewComment(comment: Comment) {
    val currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
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
    uiState = ShowDetailsUiModel(comments = currentComments)
  }

  fun deleteComment(comment: Comment) {
    var currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
    val target = currentComments.find { it.id == comment.id } ?: return

    viewModelScope.launch {
      try {
        val copy = target.copy(isLoading = true)
        currentComments.findReplace(copy) { it.id == target.id }
        uiState = ShowDetailsUiModel(comments = currentComments)

        commentsCase.delete(target)

        currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
        val targetIndex = currentComments.indexOfFirst { it.id == target.id }
        if (targetIndex > -1) {
          currentComments.removeAt(targetIndex)
          if (target.isReply()) {
            val parent = currentComments.first { it.id == target.parentId }
            val repliesCount = currentComments.count { it.parentId == parent.id }.toLong()
            currentComments.findReplace(parent.copy(replies = repliesCount)) { it.id == target.parentId }
          }
        }

        uiState = ShowDetailsUiModel(comments = currentComments)
        _messageLiveData.value = MessageEvent.info(R.string.textCommentDeleted)
      } catch (t: Throwable) {
        if (t is HttpException && t.code() == 409) {
          _messageLiveData.value = MessageEvent.error(R.string.errorCommentDelete)
        } else {
          _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        }
        uiState = ShowDetailsUiModel(comments = currentComments)
      }
    }
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = _relatedLiveData.value?.toMutableList() ?: mutableListOf()
      currentItems.findReplace(new) { it.isSameAs(new) }
      _relatedLiveData.postValue(currentItems)
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
    uiState = ShowDetailsUiModel(isPremium = settingsRepository.isPremium)
  }

  private suspend fun loadRating(show: Show, isSignedIn: Boolean) {
    if (!isSignedIn) return
    try {
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
      val rating = ratingsCase.loadRating(show)
      uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
    } catch (error: Throwable) {
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
      rethrowCancellation(error)
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.addRating(show, rating)
        val userRating = TraktRating(show.ids.trakt, rating)
        uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        Analytics.logShowRated(show, rating)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        rethrowCancellation(error)
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
        rethrowCancellation(error)
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

      announcementManager.refreshShowsAnnouncements(context)
      Analytics.logShowAddToMyShows(show)
    }
  }

  fun addWatchlistShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      watchlistCase.addToWatchlist(show)
      quickSyncManager.scheduleShowsWatchlist(context, listOf(show.traktId))

      uiState = ShowDetailsUiModel(followedState = FollowedState.inWatchlist())

      Analytics.logShowAddToWatchlistShows(show)
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

      val state = FollowedState.notFollowed()
      val event = ActionEvent(showRemoveTrakt)
      uiState = when {
        isMyShows || isArchived -> ShowDetailsUiModel(followedState = state, removeFromTraktHistory = event)
        isWatchlist -> ShowDetailsUiModel(followedState = state, removeFromTraktWatchlist = event)
        else -> error("Unexpected show state")
      }

      announcementManager.refreshShowsAnnouncements(context)
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
        rethrowCancellation(error)
      }
    }
  }

  fun removeFromTraktWatchlist() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showFromTraktLoading = true)
        watchlistCase.removeTraktWatchlist(show)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false, removeFromTraktWatchlist = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false)
        rethrowCancellation(error)
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
            quickSyncManager.scheduleEpisodes(context, listOf(episode.ids.trakt.id))
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
            quickSyncManager.scheduleEpisodes(context, episodesAdded.map { it.ids.trakt.id })
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

  private fun checkSeasonsLoaded(): Boolean {
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
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
      _seasonsLiveData.postValue(items)
    }
  }

  private suspend fun refreshWatchedEpisodes() {
    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
    _seasonsLiveData.postValue(updatedSeasonItems)
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

  fun refreshAnnouncements(context: Context) {
    viewModelScope.launch {
      val isFollowed = myShowsCase.isMyShows(show)
      if (isFollowed) {
        announcementManager.refreshShowsAnnouncements(context)
      }
    }
  }

  fun setQuickProgress(context: Context, item: QuickSetupListItem?) {
    if (item == null) return
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
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

      _messageLiveData.value = MessageEvent.info(R.string.textShowQuickProgressDone)
      Analytics.logShowQuickProgress(show)
    }
  }
}
