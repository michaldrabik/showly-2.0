package com.michaldrabik.ui_show.sections.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_people.details.PersonDetailsArgs
import com.michaldrabik.ui_show.ShowDetailsEvent
import com.michaldrabik.ui_show.sections.people.cases.ShowDetailsPeopleCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShowDetailsPeopleViewModel @Inject constructor(
  private val actorsCase: ShowDetailsPeopleCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var show: Show

  private var lastOpenedPerson: Person? = null
  private var lastOpenedPersonArgs: PersonDetailsArgs? = null

  private val loadingState = MutableStateFlow(true)
  private val actorsState = MutableStateFlow<List<Person>?>(null)
  private val crewState = MutableStateFlow<Map<Department, List<Person>>?>(null)

  fun loadPeople(show: Show) {
    if (this::show.isInitialized) return
    this.show = show

    viewModelScope.launch {
      try {
        val people = actorsCase.loadPeople(show)

        val actors = people.getOrDefault(Department.ACTING, emptyList())
        val crew = people.filter { it.key !in arrayOf(Department.ACTING, Department.UNKNOWN) }

        loadingState.value = false
        actorsState.value = actors
        crewState.value = crew

        actorsCase.preloadDetails(actors)
      } catch (error: Throwable) {
        loadingState.value = false
        actorsState.value = emptyList()
        crewState.value = emptyMap()
        rethrowCancellation(error)
      }
    }
    Timber.d("Loading people...")
  }

  fun loadPersonDetails(
    person: Person,
    personArgs: PersonDetailsArgs? = null,
  ) {
    viewModelScope.launch {
      eventChannel.send(ShowDetailsEvent.OpenPersonSheet(show, person, personArgs))
    }
  }

  fun loadPeopleList(people: List<Person>, department: Department) {
    viewModelScope.launch {
      eventChannel.send(ShowDetailsEvent.OpenPeopleSheet(show, people, department))
    }
  }

  fun loadLastPerson() {
    lastOpenedPerson?.let {
      loadPersonDetails(it, lastOpenedPersonArgs)
      lastOpenedPerson = null
      lastOpenedPersonArgs = null
    }
  }

  fun saveLastPerson(
    person: Person,
    personArgs: PersonDetailsArgs?,
  ) {
    lastOpenedPerson = person
    lastOpenedPersonArgs = personArgs
  }

  val uiState = combine(
    loadingState,
    actorsState,
    crewState
  ) { s1, s2, s3 ->
    ShowDetailsPeopleUiState(
      isLoading = s1,
      actors = s2,
      crew = s3,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsPeopleUiState()
  )
}
