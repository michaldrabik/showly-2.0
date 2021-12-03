package com.michaldrabik.ui_people

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replaceItem
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.cases.PersonDetailsCreditsCase
import com.michaldrabik.ui_people.cases.PersonDetailsImagesCase
import com.michaldrabik.ui_people.cases.PersonDetailsLoadCase
import com.michaldrabik.ui_people.recycler.PersonDetailsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
  private val loadDetailsCase: PersonDetailsLoadCase,
  private val loadCreditsCase: PersonDetailsCreditsCase,
  private val loadImagesCase: PersonDetailsImagesCase,
) : BaseViewModel() {

  private val personDetailsItemsState = MutableStateFlow<List<PersonDetailsItem>?>(null)

  private var mainJob: Job? = null
  private var mainProgressJob: Job? = null
  private var creditsJob: Job? = null
  private var creditsProgressJob: Job? = null

  fun loadDetails(person: Person) {
    mainJob?.cancel()
    mainJob = viewModelScope.launch {
      personDetailsItemsState.value = emptyList()
      mainProgressJob = launchDelayed(750) { setMainLoading(true) }
      try {
        val dateFormat = loadDetailsCase.loadDateFormat()
        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(person, dateFormat, false))
          add(PersonDetailsItem.MainBio(person.bio, person.bioTranslation))
        }

        val details = loadDetailsCase.loadDetails(person)
        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(details, dateFormat, false))
          add(PersonDetailsItem.MainBio(details.bio, details.bioTranslation))
        }
        mainProgressJob?.cancel()

        creditsProgressJob = launchDelayed(750) { setCreditsLoading(true) }
        loadCredits(details)
      } catch (error: Throwable) {
        // TODO Handle error ui
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        setMainLoading(false)
        setCreditsLoading(false)
      }
    }
  }

  fun loadCredits(person: Person, filters: List<Mode> = emptyList()) {
    creditsJob?.cancel()
    creditsJob = viewModelScope.launch {
      val current = personDetailsItemsState.value?.toMutableList()
      current?.removeIf { it.isCreditsItem() }
      personDetailsItemsState.value = current

      val credits = loadCreditsCase.loadCredits(person, filters)
      setCreditsLoading(false)
      current?.let { currentValue ->
        if (currentValue.none { it is PersonDetailsItem.CreditsFiltersItem }) {
          currentValue.add(PersonDetailsItem.CreditsFiltersItem(filters))
        }
        credits.forEach { (year, credit) ->
          currentValue.add(PersonDetailsItem.CreditsHeader(year))
          currentValue.addAll(
            credit.map { c ->
              c.show?.let { return@map PersonDetailsItem.CreditsShowItem(it, c.image) }
              c.movie?.let { return@map PersonDetailsItem.CreditsMovieItem(it, c.image) }
              throw IllegalStateException()
            }
          )
        }
        personDetailsItemsState.value = currentValue
      }
    }
  }

  fun loadMissingImage(item: PersonDetailsItem, force: Boolean) {
    viewModelScope.launch {
      (item as? PersonDetailsItem.CreditsShowItem)?.let {
        updateItem(it.copy(isLoading = true))
        val updatedItem = loadImagesCase.loadMissingImage(it, force)
        updateItem(updatedItem)
      }
      (item as? PersonDetailsItem.CreditsMovieItem)?.let {
        updateItem(it.copy(isLoading = true))
        val updatedItem = loadImagesCase.loadMissingImage(it, force)
        updateItem(updatedItem)
      }
    }
  }

  private fun updateItem(newItem: PersonDetailsItem) {
    val currentItems = personDetailsItemsState.value?.toMutableList()
    currentItems?.findReplace(newItem) { it.getId() == newItem.getId() }
    personDetailsItemsState.value = currentItems
  }

  private fun setMainLoading(isLoading: Boolean) {
    if (!isLoading) mainProgressJob?.cancel()

    val current = personDetailsItemsState.value?.toMutableList()
    current?.let { currentValue ->
      val mainInfoItem = currentValue.first { it is PersonDetailsItem.MainInfo } as PersonDetailsItem.MainInfo
      val value = mainInfoItem.copy(isLoading = isLoading)
      currentValue.replaceItem(mainInfoItem, value)
      personDetailsItemsState.value = currentValue
    }
  }

  private fun setCreditsLoading(isLoading: Boolean) {
    if (!isLoading) creditsProgressJob?.cancel()

    val current = personDetailsItemsState.value?.toMutableList()
    current?.let { currentValue ->
      if (isLoading) {
        currentValue.add(PersonDetailsItem.CreditsLoadingItem)
      } else {
        currentValue.remove(PersonDetailsItem.CreditsLoadingItem)
      }
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
