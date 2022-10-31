package com.michaldrabik.ui_show.sections.nextepisode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.sections.nextepisode.cases.ShowDetailsNextEpisodeCase
import com.michaldrabik.ui_show.sections.nextepisode.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.sections.nextepisode.helpers.NextEpisodeBundle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailsNextEpisodeViewModel @Inject constructor(
  private val nextEpisodeCase: ShowDetailsNextEpisodeCase,
  private val translationCase: ShowDetailsTranslationCase,
  private val dateFormatProvider: DateFormatProvider,
) : ViewModel() {

  private lateinit var show: Show

  private val nextEpisodeState = MutableStateFlow<NextEpisodeBundle?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadNextEpisode(show: Show) {
    if (this::show.isInitialized) return
    this.show = show
    viewModelScope.launch {
      try {
        val episode = nextEpisodeCase.loadNextEpisode(show.ids.trakt)
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
        Logger.record(error, "ShowDetailsViewModel::loadNextEpisode()")
        rethrowCancellation(error)
      }
    }
  }

  val uiState = combine(
    nextEpisodeState,
    loadingState
  ) { s1, _ ->
    ShowDetailsNextEpisodeUiState(
      nextEpisode = s1
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsNextEpisodeUiState()
  )
}
