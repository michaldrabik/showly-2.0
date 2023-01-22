package com.michaldrabik.ui_people.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replaceItem
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.details.cases.PersonDetailsCreditsCase
import com.michaldrabik.ui_people.details.cases.PersonDetailsImagesCase
import com.michaldrabik.ui_people.details.cases.PersonDetailsLoadCase
import com.michaldrabik.ui_people.details.cases.PersonDetailsTranslationsCase
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
  private val loadTranslationsCase: PersonDetailsTranslationsCase,
  private val settingsRepository: SettingsRepository,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val personDetailsItemsState = MutableStateFlow<List<PersonDetailsItem>?>(null)

  private var mainProgressJob: Job? = null
  private var creditsJob: Job? = null
  private var creditsProgressJob: Job? = null
  private var imagesJobs = mutableMapOf<String, Boolean>()
  private var translationsJobs = mutableMapOf<String, Boolean>()

  fun loadDetails(person: Person) {
    viewModelScope.launch {
      mainProgressJob = launchDelayed(750) { setMainLoading(true) }
      try {
        val dateFormat = loadDetailsCase.loadDateFormat()
        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(person, dateFormat, false))
          if (!person.bio.isNullOrBlank()) {
            add(PersonDetailsItem.MainBio(person.bio, person.bioTranslation))
          }
        }

        val details = loadDetailsCase.loadDetails(person)
        personDetailsItemsState.value = mutableListOf<PersonDetailsItem>().apply {
          add(PersonDetailsItem.MainInfo(details, dateFormat, false))
          add(PersonDetailsItem.MainBio(details.bio, details.bioTranslation))
        }
        mainProgressJob?.cancelAndJoin()

        loadCredits(details)
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        setMainLoading(false)
      }
    }
  }

  fun loadCredits(person: Person, filters: List<Mode> = emptyList()) {
    creditsJob?.cancel()
    creditsJob = viewModelScope.launch {
      creditsProgressJob = launchDelayed(500) { setCreditsLoading(true) }
      try {
        val credits = loadCreditsCase.loadCredits(person, filters)

        setCreditsLoading(false)

        val current = personDetailsItemsState.value?.toMutableList()
        current?.let { currentValue ->
          val filtersItem = PersonDetailsItem.CreditsFiltersItem(filters)
          if (currentValue.none { it is PersonDetailsItem.CreditsFiltersItem }) {
            currentValue.add(filtersItem)
          } else {
            currentValue.findReplace(filtersItem) { it is PersonDetailsItem.CreditsFiltersItem }
          }
          currentValue.removeIf { it.isCreditsItem() }
          credits.forEach { (year, credit) ->
            currentValue.add(PersonDetailsItem.CreditsHeader(year))
            currentValue.addAll(credit)
          }
          personDetailsItemsState.value = currentValue
        }
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        Timber.e(error)
        rethrowCancellation(error)
      } finally {
        setCreditsLoading(false)
      }
    }
  }

  fun loadMissingImage(item: PersonDetailsItem, force: Boolean) {
    if (item.getId() in imagesJobs.keys) {
      return
    }
    imagesJobs[item.getId()] = true
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

  fun loadMissingTranslation(item: PersonDetailsItem) {
    val language = settingsRepository.language
    if (language == Config.DEFAULT_LANGUAGE || item.getId() in translationsJobs.keys) {
      return
    }
    translationsJobs[item.getId()] = true
    viewModelScope.launch {
      (item as? PersonDetailsItem.CreditsShowItem)?.let {
        val updatedItem = loadTranslationsCase.loadMissingTranslation(it, language)
        updateItem(updatedItem)
      }
      (item as? PersonDetailsItem.CreditsMovieItem)?.let {
        val updatedItem = loadTranslationsCase.loadMissingTranslation(it, language)
        updateItem(updatedItem)
      }
    }
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

  private fun updateItem(newItem: PersonDetailsItem) {
    val currentItems = personDetailsItemsState.value?.toMutableList()
    currentItems?.findReplace(newItem) { it.getId() == newItem.getId() }
    personDetailsItemsState.value = currentItems
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
