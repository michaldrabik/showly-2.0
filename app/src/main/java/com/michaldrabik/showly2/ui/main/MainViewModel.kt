package com.michaldrabik.showly2.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.main.cases.MainInitialsCase
import com.michaldrabik.showly2.ui.main.cases.MainMiscCase
import com.michaldrabik.showly2.ui.main.cases.MainTipsCase
import com.michaldrabik.showly2.ui.main.cases.MainTraktCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase
) : BaseViewModel<MainUiModel>() {

  fun initSettings() {
    viewModelScope.launch {
      initCase.initSettings()
      checkInitialRun()
      initCase.initFcm()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
    }
    val showWhatsNew = initCase.showWhatsNew(isInitialRun)
    uiState = MainUiModel(isInitialRun = isInitialRun, showWhatsNew = showWhatsNew)
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

  fun isTipShown(tip: Tip) = tipsCase.isTipShown(tip)

  fun setTipShown(tip: Tip) = tipsCase.setTipShown(tip)

  fun clearUp() = miscCase.clear()
}
