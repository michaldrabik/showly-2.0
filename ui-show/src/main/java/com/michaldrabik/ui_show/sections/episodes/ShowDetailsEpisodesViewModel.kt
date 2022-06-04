package com.michaldrabik.ui_show.sections.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.ShowDetailsEvent
import com.michaldrabik.ui_show.sections.ratings.cases.ShowDetailsRatingCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShowDetailsEpisodesViewModel @Inject constructor(
  private val ratingsCase: ShowDetailsRatingCase,
) : ViewModel() {

  private val showState = MutableStateFlow<Show?>(null)
  private val ratingsState = MutableStateFlow<Ratings?>(null)

  fun handleEvent(event: ShowDetailsEvent<*>) {
    when (event) {
      is ShowDetailsEvent.ShowLoaded -> {
      }
      else -> Unit
    }
  }

  val uiState = combine(
    ratingsState,
    showState
  ) { s1, s2 ->
    ShowDetailsEpisodesUiState(
      ratings = s1,
      show = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsEpisodesUiState()
  )
}
