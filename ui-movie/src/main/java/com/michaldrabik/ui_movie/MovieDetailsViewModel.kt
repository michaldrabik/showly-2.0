package com.michaldrabik.ui_movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.MovieDetailsUiState.FollowedState
import com.michaldrabik.ui_movie.MovieDetailsUiState.StreamingsState
import com.michaldrabik.ui_movie.cases.MovieDetailsActorsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsCommentsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsHiddenCase
import com.michaldrabik.ui_movie.cases.MovieDetailsListsCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMainCase
import com.michaldrabik.ui_movie.cases.MovieDetailsMyMoviesCase
import com.michaldrabik.ui_movie.cases.MovieDetailsRatingCase
import com.michaldrabik.ui_movie.cases.MovieDetailsStreamingCase
import com.michaldrabik.ui_movie.cases.MovieDetailsTranslationCase
import com.michaldrabik.ui_movie.cases.MovieDetailsWatchlistCase
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
  private val actorsCase: MovieDetailsActorsCase,
  private val commentsCase: MovieDetailsCommentsCase,
  private val translationCase: MovieDetailsTranslationCase,
  private val ratingsCase: MovieDetailsRatingCase,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val watchlistCase: MovieDetailsWatchlistCase,
  private val hiddenCase: MovieDetailsHiddenCase,
  private val listsCase: MovieDetailsListsCase,
  private val streamingCase: MovieDetailsStreamingCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val imagesProvider: MovieImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val announcementManager: AnnouncementManager,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val movieState = MutableStateFlow<Movie?>(null)
  private val movieLoadingState = MutableStateFlow<Boolean?>(null)
  private val movieRatingsState = MutableStateFlow<Ratings?>(null)
  private val imageState = MutableStateFlow<Image?>(null)
  private val actorsState = MutableStateFlow<List<Person>?>(null)
  private val crewState = MutableStateFlow<Map<Person.Department, List<Person>>?>(null)
  private val commentsState = MutableStateFlow<List<Comment>?>(null)
  private val commentsDateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val followedState = MutableStateFlow<FollowedState?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val streamingsState = MutableStateFlow<StreamingsState?>(null)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val countryState = MutableStateFlow<AppCountry?>(null)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val signedInState = MutableStateFlow(false)
  private val premiumState = MutableStateFlow(false)
  private val listsCountState = MutableStateFlow(0)

  private val removeTraktEvent = MutableStateFlow<Event<Int>?>(null)
  private val finishedEvent = MutableStateFlow<Event<Boolean>?>(null)

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
        val isHidden = async { hiddenCase.isHidden(movie) }
        val isFollowed = FollowedState(
          isMyMovie = isMyMovie.await(),
          isWatchlist = isWatchlist.await(),
          isHidden = isHidden.await(),
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
        loadRating()
        launch { loadRatings(movie) }
        launch { loadCastCrew(movie) }
        launch { loadStreamings(movie) }
        launch { loadTranslation(movie) }
      } catch (error: Throwable) {
        progressJob.cancel()
        if (error is HttpException && error.code() == 404) {
          // Malformed Trakt data or duplicate show.
          messageChannel.send(MessageEvent.Info(R.string.errorMalformedMovie))
        } else {
          messageChannel.send(MessageEvent.Error(R.string.errorCouldNotLoadMovie))
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

  private suspend fun loadCastCrew(movie: Movie) {
    try {
      val people = actorsCase.loadPeople(movie)

      val actors = people.getOrDefault(Person.Department.ACTING, emptyList())
      val crew = people.filter { it.key !in arrayOf(Person.Department.ACTING, Person.Department.UNKNOWN) }

      actorsState.value = actors
      crewState.value = crew

      actorsCase.preloadDetails(actors)
    } catch (error: Throwable) {
      actorsState.value = emptyList()
      crewState.value = emptyMap()
      rethrowCancellation(error)
    }
  }

//  private suspend fun loadRelatedMovies(movie: Movie) {
//    try {
//      val (myMovies, watchlistMovies) = myMoviesCase.getAllIds()
//      val related = relatedCase.loadRelatedMovies(movie).map {
//        val image = imagesProvider.findCachedImage(it, ImageType.POSTER)
//        RelatedListItem(
//          movie = it,
//          image = image,
//          isFollowed = it.traktId in myMovies,
//          isWatchlist = it.traktId in watchlistMovies
//        )
//      }
//      relatedState.value = related
//    } catch (error: Throwable) {
//      relatedState.value = emptyList()
//      rethrowCancellation(error)
//    }
//  }

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
    commentsState.value = null
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
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
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
        messageChannel.send(MessageEvent.Info(R.string.textCommentDeleted))
      } catch (t: Throwable) {
        if (t is HttpException && t.code() == 409) {
          messageChannel.send(MessageEvent.Error(R.string.errorCommentDelete))
        } else {
          messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        }
        commentsState.value = currentComments
      }
    }
  }

//  fun loadMissingImage(item: RelatedListItem, force: Boolean) {
//
//    fun updateItem(new: RelatedListItem) {
//      val currentItems = uiState.value.relatedMovies?.toMutableList()
//      currentItems?.findReplace(new) { it isSameAs new }
//      relatedState.value = currentItems
//    }
//
//    viewModelScope.launch {
//      updateItem(item.copy(isLoading = true))
//      try {
//        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
//        updateItem(item.copy(isLoading = false, image = image))
//      } catch (t: Throwable) {
//        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
//      }
//    }
//  }

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

  fun loadRating() {
    viewModelScope.launch {
      val isSignedIn = userManager.isAuthorized()
      if (!isSignedIn) return@launch
      try {
        ratingState.value = RatingState(rateLoading = true, rateAllowed = isSignedIn)
        val rating = ratingsCase.loadRating(movie)
        ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn, userRating = rating ?: TraktRating.EMPTY)
      } catch (error: Throwable) {
        ratingState.value = RatingState(rateLoading = false, rateAllowed = isSignedIn)
        rethrowCancellation(error)
      }
    }
  }

  fun addFollowedMovie() {
    viewModelScope.launch {
      myMoviesCase.addToMyMovies(movie)
      followedState.value = FollowedState.inMyMovies()
      Analytics.logMovieAddToMyMovies(movie)
    }
  }

  fun addWatchlistMovie() {
    viewModelScope.launch {
      watchlistCase.addToWatchlist(movie)
      followedState.value = FollowedState.inWatchlist()
      Analytics.logMovieAddToWatchlistMovies(movie)
    }
  }

  fun addHiddenMovie() {
    viewModelScope.launch {
      hiddenCase.addToHidden(movie)
      followedState.value = FollowedState.inHidden()
      Analytics.logMovieAddToArchive(movie)
    }
  }

  fun removeFromFollowed() {
    viewModelScope.launch {
      val isMyMovie = myMoviesCase.isMyMovie(movie)
      val isWatchlist = watchlistCase.isWatchlist(movie)
      val isHidden = hiddenCase.isHidden(movie)

      when {
        isMyMovie -> myMoviesCase.removeFromMyMovies(movie)
        isWatchlist -> watchlistCase.removeFromWatchlist(movie)
        isHidden -> hiddenCase.removeFromHidden(movie)
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled

      val state = FollowedState.idle()
      when {
        isMyMovie -> {
          followedState.value = state
          if (showRemoveTrakt) {
            removeTraktEvent.value = Event(R.id.actionMovieDetailsFragmentToRemoveTraktProgress)
          }
        }
        isWatchlist -> {
          followedState.value = state
          if (showRemoveTrakt) {
            removeTraktEvent.value = Event(R.id.actionMovieDetailsFragmentToRemoveTraktWatchlist)
          }
        }
        isHidden -> {
          followedState.value = state
          if (showRemoveTrakt) {
            removeTraktEvent.value = Event(R.id.actionMovieDetailsFragmentToRemoveTraktHidden)
          }
        }
        else -> error("Unexpected movie state.")
      }

      announcementManager.refreshMoviesAnnouncements()
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
        finishedEvent.value = Event(true)
      }
    }
  }

  val uiState = combine(
    movieState,
    movieLoadingState,
    movieRatingsState,
    imageState,
    actorsState,
    crewState,
    commentsState,
    commentsDateFormatState,
    followedState,
    ratingState,
    streamingsState,
    translationState,
    countryState,
    dateFormatState,
    signedInState,
    premiumState,
    listsCountState,
    removeTraktEvent,
    finishedEvent
  ) { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19 ->
    MovieDetailsUiState(
      movie = s1,
      movieLoading = s2,
      ratings = s3,
      image = s4,
      actors = s5,
      crew = s6,
      comments = s7,
      commentsDateFormat = s8,
      followedState = s9,
      ratingState = s10,
      streamings = s11,
      translation = s12,
      country = s13,
      dateFormat = s14,
      isSignedIn = s15,
      isPremium = s16,
      listsCount = s17,
      removeFromTrakt = s18,
      isFinished = s19
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsUiState()
  )
}
