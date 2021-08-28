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
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.MovieDetailsUiState.FollowedState
import com.michaldrabik.ui_movie.MovieDetailsUiState.StreamingsState
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
import com.michaldrabik.ui_movie.related.RelatedListItem
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : BaseViewModel() {

  private val movieState = MutableStateFlow<Movie?>(null)
  private val movieLoadingState = MutableStateFlow<Boolean?>(null)
  private val movieRatingsState = MutableStateFlow<Ratings?>(null)
  private val imageState = MutableStateFlow<Image?>(null)
  private val actorsState = MutableStateFlow<List<Actor>?>(null)
  private val relatedState = MutableStateFlow<List<RelatedListItem>?>(null)
  private val commentsState = MutableStateFlow<List<Comment>?>(null)
  private val commentsDateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val followedState = MutableStateFlow<FollowedState?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val streamingsState = MutableStateFlow<StreamingsState?>(null)
  private val traktLoadingState = MutableStateFlow(false)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val signedInState = MutableStateFlow(false)
  private val premiumState = MutableStateFlow(false)
  private val listsCountState = MutableStateFlow(0)

  private val removeTraktHistoryEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val removeTraktWatchlistEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val finishedEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)

  private var movie by notNull<Movie>()

  fun loadDetails(id: IdTrakt) {
    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        movieLoadingState.value = true
      }
      try {
        movie = mainCase.loadDetails(id)
        Analytics.logMovieDetailsDisplay(movie)

        val isSignedIn = userManager.isAuthorized()
        val isMyMovie = async { myMoviesCase.isMyMovie(movie) }
        val isWatchlist = async { watchlistCase.isWatchlist(movie) }
        val isFollowed = FollowedState(
          isMyMovie = isMyMovie.await(),
          isWatchlist = isWatchlist.await(),
          withAnimation = false
        )

        progressJob.cancel()

        movieState.value = movie
        movieLoadingState.value = false
        followedState.value = isFollowed
        ratingState.value = RatingState(rateAllowed = isSignedIn, rateLoading = false)
        countryState.value = AppCountry.fromCode(settingsRepository.country)
        premiumState.value = settingsRepository.isPremium
        signedInState.value = isSignedIn
        dateFormatState.value = dateFormatProvider.loadShortDayFormat()
        commentsDateFormatState.value = dateFormatProvider.loadFullHourFormat()

        loadBackgroundImage(movie)
        loadListsCount(movie)
        launch { loadRatings(movie) }
        launch { loadActors(movie) }
        launch { loadStreamings(movie) }
        launch { loadRelatedMovies(movie) }
        launch { loadTranslation(movie) }
        launch { loadRating(movie, isSignedIn) }
      } catch (error: Throwable) {
        progressJob.cancel()
        if (error is HttpException && error.code() == 404) {
          // Malformed Trakt data or duplicate show.
          _messageState.emit(MessageEvent.info(R.string.errorMalformedMovie))
        } else {
          _messageState.emit(MessageEvent.error(R.string.errorCouldNotLoadMovie))
        }
        Logger.record(error, "Source" to "MovieDetailsViewModel")
        rethrowCancellation(error)
      }
    }
  }

  fun loadBackgroundImage(movie: Movie? = null) {
    viewModelScope.launch {
      try {
        val backgroundImage = imagesProvider.loadRemoteImage(movie ?: this@MovieDetailsViewModel.movie, ImageType.FANART)
        imageState.value = backgroundImage
      } catch (error: Throwable) {
        imageState.value = Image.createUnavailable(ImageType.FANART)
        Timber.e(error)
        rethrowCancellation(error)
      }
    }
  }

  private suspend fun loadActors(movie: Movie) {
    try {
      val actors = actorsCase.loadActors(movie)
      actorsState.value = actors
    } catch (error: Throwable) {
      actorsState.value = emptyList()
      rethrowCancellation(error)
    }
  }

  private suspend fun loadRelatedMovies(movie: Movie) {
    try {
      val related = relatedCase.loadRelatedMovies(movie).map {
        val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
        RelatedListItem(it, image)
      }
      relatedState.value = related
    } catch (error: Throwable) {
      relatedState.value = emptyList()
      rethrowCancellation(error)
    }
  }

  private suspend fun loadTranslation(movie: Movie) {
    try {
      translationCase.loadTranslation(movie)?.let {
        translationState.value = it
      }
    } catch (error: Throwable) {
      rethrowCancellation(error)
    }
  }

  fun loadListsCount(movie: Movie? = null) {
    viewModelScope.launch {
      val count = listsCase.countLists(movie ?: this@MovieDetailsViewModel.movie)
      listsCountState.value = count
    }
  }

  private suspend fun loadStreamings(movie: Movie) {
    try {
      val localStreamings = streamingCase.getLocalStreamingServices(movie)
      streamingsState.value = StreamingsState(localStreamings, isLocal = true)

      val remoteStreamings = streamingCase.loadStreamingServices(movie)
      streamingsState.value = StreamingsState(remoteStreamings, isLocal = false)
    } catch (error: Throwable) {
      streamingsState.value = StreamingsState(emptyList(), isLocal = false)
      rethrowCancellation(error)
    }
  }

  fun loadComments() {
    viewModelScope.launch {
      try {
        val comments = commentsCase.loadComments(movie)
        commentsState.value = comments
      } catch (error: Throwable) {
        commentsState.value = emptyList()
        Timber.e(error)
      }
    }
    Analytics.logMovieCommentsClick(movie)
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
      } catch (t: Throwable) {
        commentsState.value = currentComments
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
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
      val currentItems = uiState.value.relatedMovies?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      relatedState.value = currentItems
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
    premiumState.value = settingsRepository.isPremium
  }

  private suspend fun loadRatings(movie: Movie) {
    val traktRatings = Ratings(
      trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", movie.rating), false),
      imdb = Ratings.Value(null, true),
      metascore = Ratings.Value(null, true),
      rottenTomatoes = Ratings.Value(null, true)
    )
    try {
      movieRatingsState.value = traktRatings
      val ratings = ratingsCase.loadExternalRatings(movie)
      movieRatingsState.value = ratings
    } catch (error: Throwable) {
      movieRatingsState.value = traktRatings
      rethrowCancellation(error)
    }
  }

  private suspend fun loadRating(movie: Movie, isSignedIn: Boolean) {
    if (!isSignedIn) return
    try {
      ratingState.value = RatingState(rateLoading = true, rateAllowed = isSignedIn)
      val rating = ratingsCase.loadRating(movie)
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
        ratingsCase.addRating(movie, rating)
        val userRating = TraktRating(movie.ids.trakt, rating)
        ratingState.value = RatingState(rateLoading = false, rateAllowed = true, userRating = userRating)
        _messageState.emit(MessageEvent.info(R.string.textRateSaved))
        Analytics.logMovieRated(movie, rating)
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
        ratingsCase.deleteRating(movie)
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

  fun addFollowedMovie(context: Context) {
    viewModelScope.launch {
      myMoviesCase.addToMyMovies(movie)
      quickSyncManager.scheduleMovies(listOf(movie.traktId))
      followedState.value = FollowedState.inMyMovies()
      announcementManager.refreshMoviesAnnouncements()
      Analytics.logMovieAddToMyMovies(movie)
    }
  }

  fun addWatchlistMovie(context: Context) {
    viewModelScope.launch {
      watchlistCase.addToWatchlist(movie)
      quickSyncManager.scheduleMoviesWatchlist(listOf(movie.traktId))
      followedState.value = FollowedState.inWatchlist()
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
      when {
        isMyMovie -> {
          followedState.value = state
          removeTraktHistoryEvent.value = event
        }
        isWatchlist -> {
          followedState.value = state
          removeTraktWatchlistEvent.value = event
        }
        else -> error("Unexpected movie state.")
      }

      announcementManager.refreshMoviesAnnouncements()
    }
  }

  fun removeFromTraktHistory() {
    viewModelScope.launch {
      try {
        traktLoadingState.value = true
        myMoviesCase.removeTraktHistory(movie)

        traktLoadingState.value = false
        removeTraktHistoryEvent.value = ActionEvent(false)
        _messageState.emit(MessageEvent.info(R.string.textTraktSyncMovieRemovedFromTrakt))
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
        watchlistCase.removeTraktWatchlist(movie)

        traktLoadingState.value = false
        removeTraktWatchlistEvent.value = ActionEvent(false)
        _messageState.emit(MessageEvent.info(R.string.textTraktSyncMovieRemovedFromTrakt))
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorTraktSyncGeneral))
        traktLoadingState.value = false
        Timber.e(error)
        rethrowCancellation(error)
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
        finishedEvent.value = ActionEvent(true)
      }
    }
  }

  val uiState = combine(
    movieState,
    movieLoadingState,
    movieRatingsState,
    imageState,
    actorsState,
    relatedState,
    commentsState,
    commentsDateFormatState,
    followedState,
    ratingState,
    streamingsState,
    traktLoadingState,
    translationState,
    countryState,
    dateFormatState,
    signedInState,
    premiumState,
    listsCountState,
    removeTraktHistoryEvent,
    removeTraktWatchlistEvent,
    finishedEvent
  ) { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21 ->
    MovieDetailsUiState(
      movie = s1,
      movieLoading = s2,
      ratings = s3,
      image = s4,
      actors = s5,
      relatedMovies = s6,
      comments = s7,
      commentsDateFormat = s8,
      followedState = s9,
      ratingState = s10,
      streamings = s11,
      showFromTraktLoading = s12,
      translation = s13,
      country = s14,
      dateFormat = s15,
      isSignedIn = s16,
      isPremium = s17,
      listsCount = s18,
      removeFromTraktHistory = s19,
      removeFromTraktWatchlist = s20,
      isFinished = s21
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsUiState()
  )
}
