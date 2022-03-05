package com.michaldrabik.showly2.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.showly2.ui.main.cases.MainDeepLinksCase
import com.michaldrabik.showly2.ui.main.cases.MainInitialsCase
import com.michaldrabik.showly2.ui.main.cases.MainMiscCase
import com.michaldrabik.showly2.ui.main.cases.MainModesCase
import com.michaldrabik.showly2.ui.main.cases.MainRateAppCase
import com.michaldrabik.showly2.ui.main.cases.MainTipsCase
import com.michaldrabik.showly2.ui.main.cases.MainTraktCase
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkBundle
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_settings.helpers.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase,
  private val modesCase: MainModesCase,
  private val rateAppCase: MainRateAppCase,
  private val linksCase: MainDeepLinksCase,
) : ViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val maskState = MutableStateFlow(false)
  private val initialRunEvent = MutableStateFlow<Event<Boolean>?>(null)
  private val initialLanguageEvent = MutableStateFlow<Event<AppLanguage>?>(null)
  private val whatsNewEvent = MutableStateFlow<Event<Boolean>?>(null)
  private val rateAppEvent = MutableStateFlow<Event<Boolean>?>(null)
  private val openLinkEvent = MutableStateFlow<Event<DeepLinkBundle>?>(null)

  fun initialize() {
    viewModelScope.launch {
      checkInitialRun()
      with(initCase) {
        initializeFcm()
        preloadRatings()
        loadRemoteConfig()
        saveInstallTimestamp()
      }
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
      initCase.setInitialCountry()
    }

    val showWhatsNew = initCase.showWhatsNew(isInitialRun)

    initialRunEvent.value = Event(isInitialRun)
    whatsNewEvent.value = Event(showWhatsNew)
  }

  fun checkRateApp() {
    val showRateApp = rateAppCase.shouldShowRateApp()
    rateAppEvent.value = Event(showRateApp)
  }

  fun setLanguage(appLanguage: AppLanguage) =
    initCase.setLanguage(appLanguage)

  fun checkInitialLanguage() {
    viewModelScope.launch {
      val initialLanguage = initCase.checkInitialLanguage()
      initialLanguageEvent.value = Event(initialLanguage)
      maskState.value = true
    }
  }

  fun refreshAnnouncements() {
    viewModelScope.launch {
      miscCase.refreshAnnouncements()
    }
  }

  fun refreshTraktSyncSchedule() {
    viewModelScope.launch {
      traktCase.run {
        refreshTraktSyncSchedule()
        refreshTraktQuickSync()
      }
    }
  }

  fun setMode(mode: Mode) = modesCase.setMode(mode)
  fun getMode(): Mode = modesCase.getMode()

  fun isTipShown(tip: Tip) = tipsCase.isTipShown(tip)
  fun setTipShown(tip: Tip) = tipsCase.setTipShown(tip)

  fun moviesEnabled(): Boolean = miscCase.moviesEnabled()
  fun newsEnabled(): Boolean = miscCase.newsEnabled()

  fun completeAppRate() = rateAppCase.complete()

  fun clearMask() {
    maskState.value = false
  }

  fun openDeepLink(source: DeepLinkResolver.Source) {
    viewModelScope.launch {
      val progressJob = launchDelayed(750) {
        loadingState.value = true
        maskState.value = true
      }
      try {
        val result = when (source) {
          is DeepLinkResolver.ImdbSource -> linksCase.findById(source.id)
          is DeepLinkResolver.TmdbSource -> linksCase.findById(source.id, source.type)
          is DeepLinkResolver.TraktSource -> linksCase.findById(source.id, source.type)
        }
        loadingState.value = false
        maskState.value = false
        openLinkEvent.value = Event(result)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "MainViewModel::openDeepLink:$source")
        rethrowCancellation(error)
      } finally {
        progressJob.cancelAndJoin()
      }
    }
  }

  override fun onCleared() {
    miscCase.clear()
    super.onCleared()
  }

  val uiState = combine(
    initialRunEvent,
    initialLanguageEvent,
    whatsNewEvent,
    rateAppEvent,
    openLinkEvent,
    loadingState,
    maskState
  ) { s1, s2, s3, s4, s5, s6, s7 ->
    MainUiState(
      isInitialRun = s1,
      initialLanguage = s2,
      showWhatsNew = s3,
      showRateApp = s4,
      openLink = s5,
      isLoading = s6,
      showMask = s7
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MainUiState()
  )
}
