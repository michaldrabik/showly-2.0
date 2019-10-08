package com.michaldrabik.showly2.ui.main

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val interactor: MainInteractor
) : BaseViewModel() {

  fun initSettings() {
    viewModelScope.launch { interactor.initSettings() }
  }

  fun clearCache() = interactor.clearCache()
}
