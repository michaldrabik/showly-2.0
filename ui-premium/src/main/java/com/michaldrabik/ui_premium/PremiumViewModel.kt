package com.michaldrabik.ui_premium

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class PremiumViewModel @Inject constructor(
) : BaseViewModel<PremiumUiModel>() {

  fun loadData() {
    viewModelScope.launch {
    }
  }
}
