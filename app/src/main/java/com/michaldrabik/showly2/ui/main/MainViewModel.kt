package com.michaldrabik.showly2.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.showly2.ui.main.cases.MainInitialsCase
import com.michaldrabik.showly2.ui.main.cases.MainMiscCase
import com.michaldrabik.showly2.ui.main.cases.MainRateAppCase
import com.michaldrabik.showly2.ui.main.cases.MainTipsCase
import com.michaldrabik.showly2.ui.main.cases.MainTraktCase
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Tip
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase,
  private val rateAppCase: MainRateAppCase
) : BaseViewModel<MainUiModel>() {

  fun initialize() {
    viewModelScope.launch {
      checkInitialRun()
      initCase.initFcm()
      initCase.initRatings()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
    }
    val showWhatsNew = initCase.showWhatsNew(isInitialRun)
    val showRateApp = rateAppCase.shouldShowRateApp()
    uiState = MainUiModel(isInitialRun = isInitialRun, showWhatsNew = showWhatsNew, showRateApp = showRateApp)
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

  fun setMode(mode: Mode) = miscCase.setMode(mode)

  fun getMode(): Mode = miscCase.getMode()

  fun moviesEnabled(): Boolean = miscCase.moviesEnabled()

  fun isTipShown(tip: Tip) = tipsCase.isTipShown(tip)

  fun setTipShown(tip: Tip) = tipsCase.setTipShown(tip)

  fun completeAppRate() = rateAppCase.complete()

  fun clearUp() = miscCase.clear()
}
