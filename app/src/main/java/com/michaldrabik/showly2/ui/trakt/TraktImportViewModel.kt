package com.michaldrabik.showly2.ui.trakt

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_AUTH_ERROR
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_COMPLETE_ERROR
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_COMPLETE_SUCCESS
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_PROGRESS
import com.michaldrabik.showly2.common.trakt.TraktImportService.Companion.ACTION_IMPORT_START
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportViewModel @Inject constructor(
  private val userManager: UserTraktManager
) : BaseViewModel<TraktImportUiModel>() {

  fun invalidate() {
    viewModelScope.launch {
      val isAuthorized = userManager.isAuthorized()
      uiState = TraktImportUiModel(isAuthorized = isAuthorized)
    }
  }

  fun authorizeTrakt(authData: Uri?) {
    if (authData == null) return
    viewModelScope.launch {
      try {
        val code = authData.getQueryParameter("code")
        if (code.isNullOrBlank()) {
          throw IllegalStateException("Invalid Trakt authorization code.")
        }
        userManager.authorize(code)
        _messageLiveData.value = R.string.textTraktLoginSuccess
        invalidate()
      } catch (t: Throwable) {
        _errorLiveData.value = R.string.errorAuthorization
      }
    }
  }

  fun onBroadcastAction(action: String?) {
    when (action) {
      ACTION_IMPORT_START -> {
        _messageLiveData.value = R.string.textTraktImportStarted
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_PROGRESS -> {
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_COMPLETE_SUCCESS -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageLiveData.value = R.string.textTraktImportComplete
      }
      ACTION_IMPORT_COMPLETE_ERROR -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageLiveData.value = R.string.textTraktImportError
      }
      ACTION_IMPORT_AUTH_ERROR -> {
        viewModelScope.launch {
          userManager.revokeToken()
          _errorLiveData.value = R.string.errorTraktAuthorization
          uiState = TraktImportUiModel(isProgress = false, authError = true)
        }
      }
    }
  }
}
