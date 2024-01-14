package com.michaldrabik.ui_show.sections.ratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.sections.ratings.cases.ShowDetailsRatingCase
import com.michaldrabik.ui_show.sections.ratings.cases.ShowDetailsRatingSpoilersCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShowDetailsRatingsViewModel @Inject constructor(
  private val ratingsCase: ShowDetailsRatingCase,
  private val ratingsSpoilersCase: ShowDetailsRatingSpoilersCase,
) : ViewModel() {

  private lateinit var show: Show

  private val showState = MutableStateFlow<Show?>(null)
  private val ratingsState = MutableStateFlow<Ratings?>(null)
  private val isRefreshingRatingsState = MutableStateFlow(false)

  fun loadRatings(show: Show) {
    if (this::show.isInitialized) return
    this.show = show

    viewModelScope.launch {
      showState.value = show

      val traktRatings = Ratings(
        trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", show.rating), false),
        imdb = Ratings.Value(null, true),
        metascore = Ratings.Value(null, true),
        rottenTomatoes = Ratings.Value(null, true)
      )

      try {
        isRefreshingRatingsState.value = false
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(show, traktRatings)
        val ratings = ratingsCase.loadExternalRatings(show)
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(show, ratings)
      } catch (error: Throwable) {
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(show, traktRatings)
        rethrowCancellation(error)
      }
    }
  }

  fun refreshRatings() {
    val show = showState.value
    val ratings = ratingsState.value
    viewModelScope.launch {
      if (show != null && ratings != null) {
        isRefreshingRatingsState.value = true
        ratingsState.value = ratingsSpoilersCase.hideSpoilerRatings(show, ratings)
      }
    }
  }

  val uiState = combine(
    showState,
    ratingsState,
    isRefreshingRatingsState
  ) { s1, s2, s3 ->
    ShowDetailsRatingsUiState(
      show = s1,
      ratings = s2,
      isRefreshingRatings = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsRatingsUiState()
  )
}
