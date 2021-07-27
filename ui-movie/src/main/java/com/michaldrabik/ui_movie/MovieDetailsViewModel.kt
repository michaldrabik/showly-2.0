package com.michaldrabik.ui_movie

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_movie.cases.MovieDetailsActorsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsCommentsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsListsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMainCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRatingCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRelatedCase
import com.michaldrabik.ui_movie.cases.MovieDetailsStreamingCase
import com.michaldrabik.ui_movie.cases.MovieDetailsTranslationCase
import com.michaldrabik.ui_movie.cases.MovieDetailsWatchlistCase
import com.michaldrabik.ui_movie.helpers.StreamingsBundle
import com.michaldrabik.ui_movie.related.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
  private val mainCase: MovieDetailsMainCase,
  private val relatedCase: MovieDetailsRelatedCase,
  private val actorsCase: MovieDetailsActorsCase,
  private val commentsCase: MovieDetailsCommentsCase,
  private val translationCase: MovieDetailsTranslationCase,
  private val ratingsCase: MovieDetailsRatingCase,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val watchlistCase: MovieDetailsWatchlistCase,
  private val listsCase: MovieDetailsListsCase,
  private val streamingCase: MovieDetailsStreamingCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val quickSyncManager: QuickSyncManager,
  private val imagesProvider: MovieImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val announcementManager: AnnouncementManager,
) : BaseViewModel<MovieDetailsUiModel>() {

  private var movie by notNull<Movie>()

  fun loadDetails(id: IdTrakt) {
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
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
          withAnimation = false
        )

        progressJob.cancel()
        uiState = MovieDetailsUiModel(
          movie = movie,
          movieLoading = false,
          followedState = followedState,
          ratingState = RatingState(rateAllowed = isSignedIn, rateLoading = false),
          country = AppCountry.fromCode(settingsRepository.country),
          isPremium = settingsRepository.isPremium,
          isSignedIn = isSignedIn,
          dateFormat = dateFormatProvider.loadShortDayFormat(),
          commentsDateFormat = dateFormatProvider.loadFullHourFormat()
        )

        loadBackgroundImage(movie)
        loadListsCount(movie)
        launch { loadRatings(movie) }
        launch { loadActors(movie) }
        launch { loadStreamings(movie) }
        launch { loadRelatedMovies(movie) }
        launch { loadTranslation(movie) }
        if (isSignedIn) launch { loadRating(movie) }
      } catch (error: Throwable) {
        progressJob.cancel()
        if (error is HttpException && error.code() == 404) {
          // Malformed Trakt data or duplicate show.
          _messageLiveData.value = MessageEvent.info(R.string.errorMalformedMovie)
        } else {
          _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadMovie)
        }
        Logger.record(error, "Source" to "MovieDetailsViewModel")
        rethrowCancellation(error)
      }
    }
  }

  fun loadBackgroundImage(movie: Movie? = null) {
    viewModelScope.launch {
      uiState = try {
        val backgroundImage = imagesProvider.loadRemoteImage(movie ?: this@MovieDetailsViewModel.movie, ImageType.FANART)
        MovieDetailsUiModel(image = backgroundImage)
      } catch (error: Throwable) {
        MovieDetailsUiModel(image = Image.createUnavailable(ImageType.FANART))
      }
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

  fun loadListsCount(movie: Movie? = null) {
    viewModelScope.launch {
      val count = listsCase.countLists(movie ?: this@MovieDetailsViewModel.movie)
      uiState = MovieDetailsUiModel(listsCount = count)
    }
  }

  private suspend fun loadStreamings(movie: Movie) {
    try {
      val localStreamings = streamingCase.getLocalStreamingServices(movie)
      uiState = MovieDetailsUiModel(streamings = StreamingsBundle(localStreamings, isLocal = true))

      val remoteStreamings = streamingCase.loadStreamingServices(movie)
      uiState = MovieDetailsUiModel(streamings = StreamingsBundle(remoteStreamings, isLocal = false))
    } catch (error: Throwable) {
      uiState = MovieDetailsUiModel(streamings = StreamingsBundle(emptyList(), isLocal = false))
      rethrowCancellation(error)
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

  fun loadCommentReplies(comment: Comment) {
    var currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
    if (currentComments.any { it.parentId == comment.id }) return

    viewModelScope.launch {
      try {
        val parent = currentComments.find { it.id == comment.id }
        parent?.let { p ->
          val copy = p.copy(isLoading = true)
          currentComments.findReplace(copy) { it.id == p.id }
          uiState = MovieDetailsUiModel(comments = currentComments)
        }

        val replies = commentsCase.loadReplies(comment)

        currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
        val parentIndex = currentComments.indexOfFirst { it.id == comment.id }
        if (parentIndex > -1) currentComments.addAll(parentIndex + 1, replies)
        parent?.let {
          currentComments.findReplace(parent.copy(isLoading = false, hasRepliesLoaded = true)) { it.id == comment.id }
        }

        uiState = MovieDetailsUiModel(comments = currentComments)
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        uiState = MovieDetailsUiModel(comments = currentComments)
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
    uiState = MovieDetailsUiModel(comments = currentComments)
  }

  fun deleteComment(comment: Comment) {
    var currentComments = uiState?.comments?.toMutableList() ?: mutableListOf()
    val target = currentComments.find { it.id == comment.id } ?: return

    viewModelScope.launch {
      try {
        val copy = target.copy(isLoading = true)
        currentComments.findReplace(copy) { it.id == target.id }
        uiState = MovieDetailsUiModel(comments = currentComments)

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

        uiState = MovieDetailsUiModel(comments = currentComments)
        _messageLiveData.value = MessageEvent.info(R.string.textCommentDeleted)
      } catch (t: Throwable) {
        if (t is HttpException && t.code() == 409) {
          _messageLiveData.value = MessageEvent.error(R.string.errorCommentDelete)
        } else {
          _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        }
        uiState = MovieDetailsUiModel(comments = currentComments)
      }
    }
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

  fun loadPremium() {
    uiState = MovieDetailsUiModel(isPremium = settingsRepository.isPremium)
  }

  private suspend fun loadRating(movie: Movie) {
    try {
      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
      val rating = ratingsCase.loadRating(movie)
      uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
    } catch (error: Throwable) {
      uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = false))
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  private suspend fun loadRatings(movie: Movie) {
    val traktRatings = Ratings(
      trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", movie.rating), false),
      imdb = Ratings.Value(null, true),
      metascore = Ratings.Value(null, true),
      rottenTomatoes = Ratings.Value(null, true)
    )
    try {
      uiState = MovieDetailsUiModel(ratings = traktRatings)
      val ratings = ratingsCase.loadExternalRatings(movie)
      uiState = MovieDetailsUiModel(ratings = ratings)
    } catch (error: Throwable) {
      uiState = MovieDetailsUiModel(ratings = traktRatings)
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        uiState = MovieDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.addRating(movie, rating)
        val userRating = TraktRating(movie.ids.trakt, rating)
        uiState = MovieDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
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
    viewModelScope.launch {
      myMoviesCase.addToMyMovies(movie)
      quickSyncManager.scheduleMovies(context, listOf(movie.traktId))
      uiState = MovieDetailsUiModel(followedState = FollowedState.inMyMovies())
      announcementManager.refreshMoviesAnnouncements()
      Analytics.logMovieAddToMyMovies(movie)
    }
  }

  fun addWatchlistMovie(context: Context) {
    viewModelScope.launch {
      watchlistCase.addToWatchlist(movie)
      quickSyncManager.scheduleMoviesWatchlist(context, listOf(movie.traktId))
      uiState = MovieDetailsUiModel(followedState = FollowedState.inWatchlist())
      announcementManager.refreshMoviesAnnouncements()
      Analytics.logMovieAddToWatchlistMovies(movie)
    }
  }

  fun removeFromFollowed() {
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

      val state = FollowedState.idle()
      val event = ActionEvent(showRemoveTrakt)
      uiState = when {
        isMyMovie -> MovieDetailsUiModel(followedState = state, removeFromTraktHistory = event)
        isWatchlist -> MovieDetailsUiModel(followedState = state, removeFromTraktWatchlist = event)
        else -> error("Unexpected movie state")
      }

      announcementManager.refreshMoviesAnnouncements()
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

  fun removeMalformedMovie(id: IdTrakt) {
    viewModelScope.launch {
      try {
        mainCase.removeMalformedMovie(id)
      } catch (error: Throwable) {
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        uiState = MovieDetailsUiModel(isFinished = ActionEvent(true))
      }
    }
  }
}
