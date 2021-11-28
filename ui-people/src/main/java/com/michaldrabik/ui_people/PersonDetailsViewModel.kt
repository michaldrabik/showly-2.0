package com.michaldrabik.ui_people

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.cases.PersonDetailsLoadCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
  private val loadDetailsCase: PersonDetailsLoadCase,
  private val dateFormatProvider: DateFormatProvider
) : BaseViewModel() {

  private val personDetailsState = MutableStateFlow<Person?>(null)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadDetails(person: Person) {
    viewModelScope.launch {
      val progressJob = launchDelayed(750) {
        loadingState.value = true
      }
      try {
        dateFormatState.value = dateFormatProvider.loadShortDayFormat()
        personDetailsState.value = person

        val details = loadDetailsCase.loadDetails(person)

        personDetailsState.value = details
        loadingState.value = false
      } catch (error: Throwable) {
        // TODO Handle error ui
        rethrowCancellation(error)
      } finally {
        progressJob.cancelAndJoin()
      }
    }
  }

  val uiState = combine(
    loadingState,
    dateFormatState,
    personDetailsState
  ) { s1, s2, s3 ->
    PersonDetailsUiState(
      isLoading = s1,
      dateFormat = s2,
      personDetails = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = PersonDetailsUiState()
  )
}
