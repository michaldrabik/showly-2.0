package com.michaldrabik.ui_base.common.sheets.ratings

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.common.sheets.ratings.cases.RatingsEpisodeCase
import com.michaldrabik.ui_base.common.sheets.ratings.cases.RatingsMovieCase
import com.michaldrabik.ui_base.common.sheets.ratings.cases.RatingsSeasonCase
import com.michaldrabik.ui_base.common.sheets.ratings.cases.RatingsShowCase
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingsSheetViewModel @Inject constructor(
  private val showRatingsCase: RatingsShowCase,
  private val movieRatingsCase: RatingsMovieCase,
  private val episodeRatingsCase: RatingsEpisodeCase,
  private val seasonRatingsCase: RatingsSeasonCase,
) : BaseViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val ratingState = MutableStateFlow<TraktRating?>(null)

  fun loadRating(idTrakt: IdTrakt, type: Type) {
    viewModelScope.launch {
      val rating = when (type) {
        Type.SHOW -> showRatingsCase.loadRating(idTrakt)
        Type.MOVIE -> movieRatingsCase.loadRating(idTrakt)
        Type.EPISODE -> episodeRatingsCase.loadRating(idTrakt)
        Type.SEASON -> seasonRatingsCase.loadRating(idTrakt)
      }
      ratingState.value = rating
    }
  }

  fun saveRating(rating: Int, id: IdTrakt, type: Type) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        when (type) {
          Type.SHOW -> showRatingsCase.saveRating(id, rating)
          Type.MOVIE -> movieRatingsCase.saveRating(id, rating)
          Type.EPISODE -> episodeRatingsCase.saveRating(id, rating)
          Type.SEASON -> seasonRatingsCase.saveRating(id, rating)
        }
        _eventChannel.send(FinishUiEvent(operation = Operation.SAVE))
      } catch (error: Throwable) {
        loadingState.value = false
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
        rethrowCancellation(error)
      }
    }
  }

  fun removeRating(id: IdTrakt, type: Type) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        when (type) {
          Type.SHOW -> showRatingsCase.deleteRating(id)
          Type.MOVIE -> movieRatingsCase.deleteRating(id)
          Type.EPISODE -> episodeRatingsCase.deleteRating(id)
          Type.SEASON -> seasonRatingsCase.deleteRating(id)
        }
        _eventChannel.send(FinishUiEvent(operation = Operation.REMOVE))
      } catch (error: Throwable) {
        loadingState.value = false
        _messageChannel.send(MessageEvent.error(R.string.errorGeneral))
        rethrowCancellation(error)
      }
    }
  }

  val uiState = combine(
    loadingState,
    ratingState
  ) { s1, s2 ->
    RatingsUiState(
      isLoading = s1,
      rating = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = RatingsUiState()
  )
}
