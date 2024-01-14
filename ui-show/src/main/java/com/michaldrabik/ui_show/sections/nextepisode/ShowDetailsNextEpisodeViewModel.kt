package com.michaldrabik.ui_show.sections.nextepisode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_show.sections.nextepisode.cases.ShowDetailsNextEpisodeCase
import com.michaldrabik.ui_show.sections.nextepisode.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.sections.nextepisode.cases.ShowDetailsWatchedCase
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
  private val watchedCase: ShowDetailsWatchedCase,
  private val spoilersSettingsRepository: SettingsSpoilersRepository,
  private val dateFormatProvider: DateFormatProvider,
) : ViewModel() {

  private lateinit var show: Show

  private val nextEpisodeState = MutableStateFlow<NextEpisodeBundle?>(null)
  private val spoilersState = MutableStateFlow<SpoilersSettings?>(null)

  fun loadNextEpisode(show: Show) {
    if (this::show.isInitialized) return
    this.show = show
    viewModelScope.launch {
      try {
        val dateFormat = dateFormatProvider.loadFullHourFormat()
        val episode = nextEpisodeCase.loadNextEpisode(show.ids.trakt)
        episode?.let {
          val isWatched = watchedCase.isWatched(show, it)
          val nextEpisode = NextEpisodeBundle(
            nextEpisode = Pair(show, it),
            dateFormat = dateFormat,
            isWatched = isWatched
          )
          spoilersState.value = spoilersSettingsRepository.getAll()
          nextEpisodeState.value = nextEpisode

          val translation = translationCase.loadTranslation(episode, show)
          if (translation?.title?.isNotBlank() == true) {
            val translated = it.copy(title = translation.title)
            val nextEpisodeTranslated = NextEpisodeBundle(
              nextEpisode = Pair(show, translated),
              dateFormat = dateFormat,
              isWatched = isWatched
            )
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
    spoilersState
  ) { s1, s2 ->
    ShowDetailsNextEpisodeUiState(
      nextEpisode = s1,
      spoilersSettings = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsNextEpisodeUiState()
  )
}
