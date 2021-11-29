package com.michaldrabik.ui_people

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replaceItem
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.cases.PersonDetailsCreditsCase
import com.michaldrabik.ui_people.cases.PersonDetailsLoadCase
import com.michaldrabik.ui_people.recycler.PersonDetailsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
  private val loadDetailsCase: PersonDetailsLoadCase,
  private val loadCreditsCase: PersonDetailsCreditsCase,
) : BaseViewModel() {

  private val personDetailsItemsState = MutableStateFlow<List<PersonDetailsItem>?>(null)

  fun loadDetails(person: Person) {
    viewModelScope.launch {
      val progressJob = launchDelayed(750) { setLoading(true) }
      try {
        val dateFormat = loadDetailsCase.loadDateFormat()
        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(person, dateFormat, false))
          if (!person.bio.isNullOrBlank()) {
            add(PersonDetailsItem.MainBio(person.bio, person.bioTranslation))
          }
        }

        val detailsAsync = async { loadDetailsCase.loadDetails(person) }
        val creditsAsync = async { loadCreditsCase.loadCredits(person) }
        val details = detailsAsync.await()
        val credits = creditsAsync.await()

        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(details, dateFormat, false))
          if (!details.bio.isNullOrBlank()) {
            add(PersonDetailsItem.MainBio(details.bio, details.bioTranslation))
          }
          addAll(
            credits.map { credit ->
              credit.show?.let { return@map PersonDetailsItem.CreditsShowItem(it, credit.image) }
              credit.movie?.let { return@map PersonDetailsItem.CreditsMovieItem(it, credit.image) }
              throw IllegalStateException()
            }
          )
        }
      } catch (error: Throwable) {
        // TODO Handle error ui
        setLoading(false)
        rethrowCancellation(error)
      } finally {
        progressJob.cancelAndJoin()
      }
    }
  }

  private fun setLoading(isLoading: Boolean) {
    val current = personDetailsItemsState.value?.toMutableList()
    current?.let { currentValue ->
      val mainInfoItem = currentValue.first { it is PersonDetailsItem.MainInfo } as PersonDetailsItem.MainInfo
      val value = mainInfoItem.copy(isLoading = isLoading)
      currentValue.replaceItem(mainInfoItem, value)
      personDetailsItemsState.value = currentValue
    }
  }

  val uiState = combine(
    personDetailsItemsState
  ) { s1 ->
    PersonDetailsUiState(
      personDetailsItems = s1[0]
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = PersonDetailsUiState()
  )
}
