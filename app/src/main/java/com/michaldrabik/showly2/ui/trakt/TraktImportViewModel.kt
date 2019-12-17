package com.michaldrabik.showly2.ui.trakt

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_AUTH_ERROR
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_COMPLETE
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_PROGRESS
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_START
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportViewModel @Inject constructor(
  private val userManager: UserTraktManager
) : BaseViewModel<TraktImportUiModel>() {

  fun onBroadcastAction(action: String?) {
    when (action) {
      ACTION_IMPORT_START -> {
        _messageStream.value = R.string.textTraktImportStarted
      }
      ACTION_IMPORT_PROGRESS -> {
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_COMPLETE -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageStream.value = R.string.textTraktImportComplete
      }
      ACTION_IMPORT_AUTH_ERROR -> {
        viewModelScope.launch {
          userManager.revokeToken()
          _errorStream.value = R.string.errorTraktAuthorization
          uiState = TraktImportUiModel(isProgress = false, authError = true)
        }
      }
    }
  }
}
