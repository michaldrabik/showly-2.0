package com.michaldrabik.showly2.ui.trakt.exports

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.TraktExportError
import com.michaldrabik.showly2.common.events.TraktExportProgress
import com.michaldrabik.showly2.common.events.TraktExportStart
import com.michaldrabik.showly2.common.events.TraktExportSuccess
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktExportViewModel @Inject constructor() : BaseViewModel<TraktExportUiModel>() {

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {
        is TraktExportStart -> {
          _messageLiveData.value = R.string.textTraktExportStarted
          uiState = TraktExportUiModel(isProgress = true)
        }
        is TraktExportProgress -> {
          uiState = TraktExportUiModel(isProgress = true)
        }
        is TraktExportSuccess -> {
          uiState = TraktExportUiModel(isProgress = false)
          _messageLiveData.value = R.string.textTraktExportComplete
        }
        is TraktExportError -> {
          uiState = TraktExportUiModel(isProgress = false)
          _messageLiveData.value = R.string.textTraktExportError
        }
      }
    }
  }
}
