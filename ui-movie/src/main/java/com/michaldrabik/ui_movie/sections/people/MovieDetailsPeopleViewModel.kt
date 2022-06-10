package com.michaldrabik.ui_movie.sections.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_movie.MovieDetailsEvent
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPeopleSheet
import com.michaldrabik.ui_movie.MovieDetailsEvent.OpenPersonSheet
import com.michaldrabik.ui_movie.MovieDetailsEvent.SaveOpenedPerson
import com.michaldrabik.ui_movie.sections.people.cases.MovieDetailsActorsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MovieDetailsPeopleViewModel @Inject constructor(
  private val actorsCase: MovieDetailsActorsCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private lateinit var movie: Movie
  private var lastOpenedPerson: Person? = null

  private val loadingState = MutableStateFlow(true)
  private val actorsState = MutableStateFlow<List<Person>?>(null)
  private val crewState = MutableStateFlow<Map<Department, List<Person>>?>(null)

  fun handleEvent(event: MovieDetailsEvent<*>) {
    when (event) {
      is SaveOpenedPerson -> {
        lastOpenedPerson = event.person
      }
      else -> Unit
    }
  }

  fun loadPeople(movie: Movie) {
    if (this::movie.isInitialized) return
    this.movie = movie

    viewModelScope.launch {
      try {
        val people = actorsCase.loadPeople(movie)

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

  fun loadPersonDetails(person: Person) {
    viewModelScope.launch {
      eventChannel.send(OpenPersonSheet(movie, person))
    }
  }

  fun loadPeopleList(people: List<Person>, department: Department) {
    viewModelScope.launch {
      eventChannel.send(OpenPeopleSheet(movie, people, department))
    }
  }

  fun loadLastPerson() {
    lastOpenedPerson?.let {
      loadPersonDetails(it)
      lastOpenedPerson = null
    }
  }

  val uiState = combine(
    loadingState,
    actorsState,
    crewState
  ) { s1, s2, s3 ->
    MovieDetailsPeopleUiState(
      isLoading = s1,
      actors = s2,
      crew = s3,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsPeopleUiState()
  )
}
