package com.michaldrabik.ui_movie

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_movie.cases.MovieDetailsActorsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsCommentsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMainCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRatingCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.cases.MovieDetailsTranslationCase
import com.michaldrabik.ui_movie.cases.MovieDetailsWatchlistCase
import com.michaldrabik.ui_movie.related.RelatedListItem
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class MovieDetailsViewModel @Inject constructor(
  private val mainCase: MovieDetailsMainCase,
  private val relatedCase: MovieDetailsRelatedCase,
  private val actorsCase: MovieDetailsActorsCase,
  private val commentsCase: MovieDetailsCommentsCase,
  private val translationCase: MovieDetailsTranslationCase,
  private val ratingsCase: MovieDetailsRatingCase,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val watchlistCase: MovieDetailsWatchlistCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val quickSyncManager: QuickSyncManager,
  private val imagesProvider: MovieImagesProvider,
  private val announcementManager: AnnouncementManager
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
        val isFollowed = async { myMoviesCase.isMyMovie(movie) }
        val isWatchlist = async { watchlistCase.isWatchlist(movie) }
        val followedState = FollowedState(
          isMyMovie = isFollowed.await(),
          isWatchlist = isWatchlist.await(),
          isUpcoming = movie.released != null && !movie.hasAired(),
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
        launch { loadActors(movie) }
        launch { loadRelatedMovies(movie) }
        launch { loadTranslation(movie) }

        if (followedState.isWatchlist) {
          announcementManager.refreshMoviesAnnouncements(context)
        }

        if (isSignedIn) launch { loadRating(movie) }
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadMovie)
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

  private suspend fun loadActors(movie: Movie) {
    uiState = try {
      val actors = actorsCase.loadActors(movie)
      MovieDetailsUiModel(actors = actors)
    } catch (t: Throwable) {
      MovieDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadRelatedMovies(movie: Movie) {
    uiState = try {
      val related = relatedCase.loadRelatedMovies(movie).map {
        val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
        RelatedListItem(it, image)
      }
      MovieDetailsUiModel(relatedMovies = related)
    } catch (t: Throwable) {
      MovieDetailsUiModel(relatedMovies = emptyList())
    }
  }

  private suspend fun loadTranslation(movie: Movie) {
    try {
      val translation = translationCase.loadTranslation(movie)
      translation?.let {
        uiState = MovieDetailsUiModel(translation = it)
      }
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "${MovieDetailsViewModel::class.simpleName}::loadTranslation()")
    }
  }

  fun loadComments() {
    viewModelScope.launch {
      uiState = try {
        val comments = commentsCase.loadComments(movie)
        MovieDetailsUiModel(comments = comments)
      } catch (t: Throwable) {
        MovieDetailsUiModel(comments = emptyList())
      }
    }
    Analytics.logMovieCommentsClick(movie)
  }

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

  private suspend fun loadRating(movie: Movie) {
    try {
      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
      val rating = ratingsCase.loadRating(movie)
      uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
    } catch (error: Throwable) {
      Timber.e(error)
      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.addRating(movie, rating)
        val userRating = TraktRating(movie.ids.trakt, rating)
        uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRated)
        Analytics.logMovieRated(movie, rating)
      } catch (error: Throwable) {
        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun deleteRating() {
    viewModelScope.launch {
      try {
        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.deleteRating(movie)
        uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = TraktRating.EMPTY, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRatingDeleted)
      } catch (error: Throwable) {
        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun addFollowedMovie(context: Context) {
    if (movie.released != null && !movie.hasAired()) {
      _messageLiveData.value = MessageEvent.info(R.string.textMovieNotYetReleased)
      return
    }
    viewModelScope.launch {
      myMoviesCase.addToMyMovies(movie)
      quickSyncManager.scheduleMovies(context, listOf(movie.traktId))
      uiState = MovieDetailsUiModel(followedState = FollowedState.inMyMovies())
      announcementManager.refreshMoviesAnnouncements(context)
      Analytics.logMovieAddToMyMovies(movie)
    }
  }

  fun addWatchlistMovie(context: Context) {
    viewModelScope.launch {
      watchlistCase.addToWatchlist(movie)
      quickSyncManager.scheduleMoviesWatchlist(context, listOf(movie.traktId))
      uiState = MovieDetailsUiModel(followedState = FollowedState.inWatchlist())
      announcementManager.refreshMoviesAnnouncements(context)
      Analytics.logMovieAddToWatchlistMovies(movie)
    }
  }

  fun removeFromFollowed(context: Context) {
    viewModelScope.launch {
      val isMyMovie = myMoviesCase.isMyMovie(movie)
      val isWatchlist = watchlistCase.isWatchlist(movie)

      when {
        isMyMovie -> {
          myMoviesCase.removeFromMyMovies(movie)
          quickSyncManager.clearMovies(listOf(movie.traktId))
        }
        isWatchlist -> {
          watchlistCase.removeFromWatchlist(movie)
          quickSyncManager.clearMoviesWatchlist(listOf(movie.traktId))
        }
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled

      val state = if (movie.hasAired()) FollowedState.notFollowed() else FollowedState.upcoming()
      val event = ActionEvent(showRemoveTrakt)
      uiState = when {
        isMyMovie -> MovieDetailsUiModel(followedState = state, removeFromTraktHistory = event)
        isWatchlist -> MovieDetailsUiModel(followedState = state, removeFromTraktWatchlist = event)
        else -> error("Unexpected movie state")
      }

      announcementManager.refreshMoviesAnnouncements(context)
    }
  }

  fun removeFromTraktHistory() {
    viewModelScope.launch {
      try {
        uiState = MovieDetailsUiModel(showFromTraktLoading = true)
        myMoviesCase.removeTraktHistory(movie)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncMovieRemovedFromTrakt)
        uiState = MovieDetailsUiModel(showFromTraktLoading = false, removeFromTraktHistory = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = MovieDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }

  fun removeFromTraktWatchlist() {
    viewModelScope.launch {
      try {
        uiState = MovieDetailsUiModel(showFromTraktLoading = true)
        watchlistCase.removeTraktWatchlist(movie)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncMovieRemovedFromTrakt)
        uiState = MovieDetailsUiModel(showFromTraktLoading = false, removeFromTraktWatchlist = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = MovieDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }
}
