package com.michaldrabik.showly2.ui.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.showly2.ui.main.cases.MainInitialsCase
import com.michaldrabik.showly2.ui.main.cases.MainMiscCase
import com.michaldrabik.showly2.ui.main.cases.MainModesCase
import com.michaldrabik.showly2.ui.main.cases.MainRateAppCase
import com.michaldrabik.showly2.ui.main.cases.MainTipsCase
import com.michaldrabik.showly2.ui.main.cases.MainTraktCase
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_settings.helpers.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase,
  private val modesCase: MainModesCase,
  private val rateAppCase: MainRateAppCase,
) : BaseViewModel() {

  private val initialRunEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val initialLanguageEvent = MutableStateFlow<ActionEvent<AppLanguage>?>(null)
  private val whatsNewEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val rateAppEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)

  val uiState = combine(
    initialRunEvent,
    initialLanguageEvent,
    whatsNewEvent,
    rateAppEvent
  ) { s1, s2, s3, s4 ->
    MainUiState(
      isInitialRun = s1,
      initialLanguage = s2,
      showWhatsNew = s3,
      showRateApp = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MainUiState()
  )

  fun initialize() {
    viewModelScope.launch {
      checkInitialRun()
      initCase.initializeFcm()
      initCase.preloadRatings()
      initCase.loadRemoteConfig()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
      initCase.setInitialCountry()
    }

    val showWhatsNew = initCase.showWhatsNew(isInitialRun)
    val showRateApp = rateAppCase.shouldShowRateApp()

    initialRunEvent.value = ActionEvent(isInitialRun)
    whatsNewEvent.value = ActionEvent(showWhatsNew)
    rateAppEvent.value = ActionEvent(showRateApp)
  }

  fun setLanguage(appLanguage: AppLanguage) =
    initCase.setLanguage(appLanguage)

  fun checkInitialLanguage() {
    val initialLanguage = initCase.checkInitialLanguage()
    initialLanguageEvent.value = ActionEvent(initialLanguage)
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

  override fun onCleared() {
    miscCase.clear()
    super.onCleared()
  }
}
