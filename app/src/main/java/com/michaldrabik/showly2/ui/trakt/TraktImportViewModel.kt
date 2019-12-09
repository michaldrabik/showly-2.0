package com.michaldrabik.showly2.ui.trakt

import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_COMPLETE
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_PROGRESS
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_START
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject

class TraktImportViewModel @Inject constructor() : BaseViewModel<TraktImportUiModel>() {

  fun onBroadcastAction(action: String?) {
    when (action) {
      ACTION_IMPORT_START, ACTION_IMPORT_PROGRESS -> {
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_COMPLETE -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageStream.value = R.string.textTraktImportComplete
      }
    }
  }
}
