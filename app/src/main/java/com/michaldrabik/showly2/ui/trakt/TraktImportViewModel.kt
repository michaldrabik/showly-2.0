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
import javax.inject.Inject
import kotlinx.coroutines.launch

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
        _messageStream.value = R.string.textTraktLoginSuccess
        invalidate()
      } catch (t: Throwable) {
        _errorStream.value = R.string.errorAuthorization
      }
    }
  }

  fun onBroadcastAction(action: String?) {
    when (action) {
      ACTION_IMPORT_START -> {
        _messageStream.value = R.string.textTraktImportStarted
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_PROGRESS -> {
        uiState = TraktImportUiModel(isProgress = true)
      }
      ACTION_IMPORT_COMPLETE_SUCCESS -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageStream.value = R.string.textTraktImportComplete
      }
      ACTION_IMPORT_COMPLETE_ERROR -> {
        uiState = TraktImportUiModel(isProgress = false)
        _messageStream.value = R.string.textTraktImportError
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
