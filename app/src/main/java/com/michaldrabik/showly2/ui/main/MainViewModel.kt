package com.michaldrabik.showly2.ui.main

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
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase,
  private val modesCase: MainModesCase,
  private val rateAppCase: MainRateAppCase,
) : BaseViewModel<MainUiModel>() {

  fun initialize(context: Context) {
    viewModelScope.launch {
      checkInitialRun(context)
      initCase.initializeFcm()
      initCase.preloadRatings()
    }
  }

  private suspend fun checkInitialRun(context: Context) {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
      initCase.setInitialCountry(context)
    }

    val showWhatsNew = initCase.showWhatsNew(isInitialRun)
    val showRateApp = rateAppCase.shouldShowRateApp()

    uiState = MainUiModel(
      isInitialRun = ActionEvent(isInitialRun),
      showWhatsNew = ActionEvent(showWhatsNew),
      showRateApp = ActionEvent(showRateApp)
    )
  }

  fun setLanguage(appLanguage: AppLanguage) =
    initCase.setLanguage(appLanguage)

  fun checkLanguageChange(isInitialRun: Boolean) {
    if (!isInitialRun) return

    val initialLanguage = initCase.checkLanguageChange()
    if (initialLanguage != AppLanguage.ENGLISH) {
      uiState = MainUiModel(initialLanguage = ActionEvent(initialLanguage))
    }
  }

  fun refreshAnnouncements(context: Context) {
    viewModelScope.launch {
      miscCase.refreshAnnouncements(context)
    }
  }

  fun refreshTraktSyncSchedule(context: Context) {
    viewModelScope.launch {
      traktCase.run {
        refreshTraktSyncSchedule(context)
        refreshTraktQuickSync(context)
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
