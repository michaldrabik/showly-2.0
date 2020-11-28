package com.michaldrabik.ui_movie

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_movie.cases.MovieDetailsMainCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.related.RelatedListItem
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class MovieDetailsViewModel @Inject constructor(
  private val mainCase: MovieDetailsMainCase,
  private val relatedCase: MovieDetailsRelatedCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: MovieImagesProvider
) : BaseViewModel<MovieDetailsUiModel>() {

  private var movie by notNull<Movie>()

  fun loadDetails(id: IdTrakt, context: Context) {
    viewModelScope.launch {
      val progressJob = launchDelayed(500) {
        uiState = MovieDetailsUiModel(movieLoading = true)
      }
      try {
        movie = mainCase.loadDetails(id)
        Analytics.logMovieDetailsDisplay(movie)

        val isSignedIn = userManager.isAuthorized()
//        val isFollowed = async { myShowsCase.isMyShows(movie) }
//        val isWatchLater = async { watchlistCase.isWatchlist(movie) }
//        val isArchived = async { archiveCase.isArchived(movie) }
        val followedState = FollowedState(
          isMyMovie = false,
          isWatchlist = false,
          isArchived = false,
          withAnimation = false
        )

        progressJob.cancel()
        uiState = MovieDetailsUiModel(
          movie = movie,
          movieLoading = false,
          followedState = followedState,
          ratingState = RatingState(rateAllowed = isSignedIn, rateLoading = false)
        )

        launch { loadBackgroundImage(movie) }
//        launch { loadActors(movie) }
        launch { loadRelatedMovies(movie) }
//        launch { loadTranslation(movie) }
//        if (isSignedIn) launch { loadRating(movie) }
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadShow)
        progressJob.cancel()
      }
    }
  }

  private suspend fun loadBackgroundImage(movie: Movie) {
    uiState = try {
      val backgroundImage = imagesProvider.loadRemoteImage(movie, ImageType.FANART)
      MovieDetailsUiModel(image = backgroundImage)
    } catch (t: Throwable) {
      MovieDetailsUiModel(image = Image.createUnavailable(ImageType.FANART))
    }
  }

  //
//  private suspend fun loadActors(show: Show) {
//    uiState = try {
//      val actors = actorsCase.loadActors(show)
//      MovieDetailsUiModel(actors = actors)
//    } catch (t: Throwable) {
//      MovieDetailsUiModel(actors = emptyList())
//    }
//  }
//
  private suspend fun loadRelatedMovies(movie: Movie) {
    uiState = try {
      val relatedShows = relatedCase.loadRelatedMovies(movie).map {
        val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
        RelatedListItem(it, image)
      }
      MovieDetailsUiModel(relatedMovies = relatedShows)
    } catch (t: Throwable) {
      MovieDetailsUiModel(relatedMovies = emptyList())
    }
  }

  //
//  private suspend fun loadTranslation(show: Show) {
//    try {
//      val translation = translationCase.loadTranslation(show)
//      translation?.let {
//        uiState = MovieDetailsUiModel(translation = it)
//      }
//    } catch (error: Throwable) {
//      Timber.e(error)
//      FirebaseCrashlytics.getInstance().recordException(error)
//    }
//  }
//
//  fun loadComments() {
//    viewModelScope.launch {
//      uiState = try {
//        val comments = commentsCase.loadComments(movie)
//        MovieDetailsUiModel(comments = comments)
//      } catch (t: Throwable) {
//        MovieDetailsUiModel(comments = emptyList())
//      }
//    }
//    Analytics.logShowCommentsClick(movie)
//  }
//
  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState?.relatedMovies?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(relatedMovies = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }
//
//  private suspend fun loadRating(show: Show) {
//    try {
//      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
//      val rating = ratingsCase.loadRating(show)
//      uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
//    } catch (error: Throwable) {
//      Timber.e(error)
//      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
//    }
//  }
//
//  fun addRating(rating: Int) {
//    viewModelScope.launch {
//      try {
//        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
//        ratingsCase.addRating(movie, rating)
//        val userRating = TraktRating(movie.ids.trakt, rating)
//        uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
//        _messageLiveData.value = MessageEvent.info(R.string.textShowRated)
//        Analytics.logShowRated(movie, rating)
//      } catch (error: Throwable) {
//        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
//        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
//      }
//    }
//  }
//
//  fun deleteRating() {
//    viewModelScope.launch {
//      try {
//        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
//        ratingsCase.deleteRating(movie)
//        uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = TraktRating.EMPTY, rateLoading = false))
//        _messageLiveData.value = MessageEvent.info(R.string.textShowRatingDeleted)
//      } catch (error: Throwable) {
//        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
//        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
//      }
//    }
//  }
//
//  fun addFollowedShow(context: Context) {
//    if (!checkSeasonsLoaded()) return
//    viewModelScope.launch {
//      val seasons = seasonItems.map { it.season }
//      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }
//      myShowsCase.addToMyShows(movie, seasons, episodes)
//
//      uiState = MovieDetailsUiModel(followedState = FollowedState.inMyShows())
//
//      announcementManager.refreshEpisodesAnnouncements(context)
//      Analytics.logShowAddToMyShows(movie)
//    }
//  }
//
//  fun addWatchlistShow(context: Context) {
//    if (!checkSeasonsLoaded()) return
//    viewModelScope.launch {
//      watchlistCase.addToWatchlist(movie)
//      quickSyncManager.scheduleShowsWatchlist(context, listOf(movie.traktId))
//
//      uiState = MovieDetailsUiModel(followedState = FollowedState.inWatchlist())
//
//      Analytics.logShowAddToWatchlistShows(movie)
//    }
//  }
//
//  fun addArchiveShow() {
//    if (!checkSeasonsLoaded()) return
//    viewModelScope.launch {
//      archiveCase.addToArchive(movie, removeLocalData = !areSeasonsLocal)
//      uiState = MovieDetailsUiModel(followedState = FollowedState.inArchive())
//      Analytics.logShowAddToArchive(movie)
//    }
//  }
//
//  fun removeFromFollowed(context: Context) {
//    if (!checkSeasonsLoaded()) return
//    viewModelScope.launch {
//      val isMyShows = myShowsCase.isMyShows(movie)
//      val isWatchlist = watchlistCase.isWatchlist(movie)
//      val isArchived = archiveCase.isArchived(movie)
//
//      when {
//        isMyShows -> {
//          myShowsCase.removeFromMyShows(movie, removeLocalData = !areSeasonsLocal)
//        }
//        isWatchlist -> {
//          watchlistCase.removeFromWatchlist(movie)
//          quickSyncManager.clearShowsWatchlist(listOf(movie.traktId))
//        }
//        isArchived -> {
//          archiveCase.removeFromArchive(movie)
//        }
//      }
//
//      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
//      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal
//
//      val state = FollowedState.notFollowed()
//      val event = ActionEvent(showRemoveTrakt)
//      uiState = when {
//        isMyShows || isArchived -> MovieDetailsUiModel(followedState = state, removeFromTraktHistory = event)
//        isWatchlist -> MovieDetailsUiModel(followedState = state, removeFromTraktWatchlist = event)
//        else -> error("Unexpected show state")
//      }
//
//      announcementManager.refreshEpisodesAnnouncements(context)
//    }
//  }
//
//  fun removeFromTraktHistory() {
//    viewModelScope.launch {
//      try {
//        uiState = MovieDetailsUiModel(showFromTraktLoading = true)
//        myShowsCase.removeTraktHistory(movie)
//        refreshWatchedEpisodes()
//        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
//        uiState = MovieDetailsUiModel(showFromTraktLoading = false, removeFromTraktHistory = ActionEvent(false))
//      } catch (error: Throwable) {
//        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
//        uiState = MovieDetailsUiModel(showFromTraktLoading = false)
//      }
//    }
//  }
//
//  fun removeFromTraktWatchlist() {
//    viewModelScope.launch {
//      try {
//        uiState = MovieDetailsUiModel(showFromTraktLoading = true)
//        watchlistCase.removeTraktWatchlist(movie)
//        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
//        uiState = MovieDetailsUiModel(showFromTraktLoading = false, removeFromTraktWatchlist = ActionEvent(false))
//      } catch (error: Throwable) {
//        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
//        uiState = MovieDetailsUiModel(showFromTraktLoading = false)
//      }
//    }
//  }
//
//  fun setWatchedEpisode(
//    context: Context,
//    episode: Episode,
//    season: Season,
//    isChecked: Boolean
//  ) {
//    viewModelScope.launch {
//      val bundle = EpisodeBundle(episode, season, movie)
//      when {
//        isChecked -> {
//          episodesManager.setEpisodeWatched(bundle)
//          if (myShowsCase.isMyShows(movie) || watchlistCase.isWatchlist(movie) || archiveCase.isArchived(movie)) {
//            quickSyncManager.scheduleEpisodes(context, listOf(episode.ids.trakt.id))
//          }
//        }
//        else -> episodesManager.setEpisodeUnwatched(bundle)
//      }
//      refreshWatchedEpisodes()
//    }
//  }
//
//  fun setWatchedSeason(context: Context, season: Season, isChecked: Boolean) {
//    viewModelScope.launch {
//      val bundle = SeasonBundle(season, movie)
//      when {
//        isChecked -> {
//          val episodesAdded = episodesManager.setSeasonWatched(bundle)
//          if (myShowsCase.isMyShows(movie) || watchlistCase.isWatchlist(movie) || archiveCase.isArchived(movie)) {
//            quickSyncManager.scheduleEpisodes(context, episodesAdded.map { it.ids.trakt.id })
//          }
//        }
//        else -> episodesManager.setSeasonUnwatched(bundle)
//      }
//      refreshWatchedEpisodes()
//    }
//  }
//
//  private suspend fun refreshWatchedEpisodes() {
//    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
//    uiState = MovieDetailsUiModel(seasons = updatedSeasonItems)
//  }
//
//  private suspend fun markWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> {
//    val items = mutableListOf<SeasonListItem>()
//
//    val watchedSeasonsIds = episodesManager.getWatchedSeasonsIds(movie)
//    val watchedEpisodesIds = episodesManager.getWatchedEpisodesIds(movie)
//
//    seasonsList.forEach { item ->
//      val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
//      val episodes = item.episodes.map { episodeItem ->
//        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
//        EpisodeListItem(episodeItem.episode, item.season, isEpisodeWatched, episodeItem.translation)
//      }
//      val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
//      items.add(updated)
//    }
//
//    seasonItems.replace(items)
//    return items
//  }
//
//  fun setQuickProgress(context: Context, item: QuickSetupListItem?) {
//    if (item == null) return
//    if (!areSeasonsLoaded) {
//      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
//      return
//    }
//    viewModelScope.launch {
//      episodesManager.setAllUnwatched(movie)
//      val seasons = seasonItems.map { it.season }
//      seasons
//        .filter { it.number < item.season.number }
//        .forEach { season ->
//          setWatchedSeason(context, season, true)
//        }
//
//      val season = seasons.find { it.number == item.season.number }
//      season?.episodes
//        ?.filter { it.number <= item.episode.number }
//        ?.forEach { episode ->
//          setWatchedEpisode(context, episode, season, true)
//        }
//
//      _messageLiveData.value = MessageEvent.info(R.string.textShowQuickProgressDone)
//      Analytics.logShowQuickProgress(movie)
//    }
//  }
}
