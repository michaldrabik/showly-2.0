package com.michaldrabik.showly2.ui.trakt.imports

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.Event
import com.michaldrabik.showly2.common.events.TraktImportAuthError
import com.michaldrabik.showly2.common.events.TraktImportError
import com.michaldrabik.showly2.common.events.TraktImportProgress
import com.michaldrabik.showly2.common.events.TraktImportStart
import com.michaldrabik.showly2.common.events.TraktImportSuccess
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

  fun handleEvent(event: Event) {
    viewModelScope.launch {
      when (event) {
        is TraktImportStart -> {
          _messageLiveData.value = R.string.textTraktImportStarted
          uiState = TraktImportUiModel(isProgress = true)
        }
        is TraktImportProgress -> {
          uiState = TraktImportUiModel(isProgress = true)
        }
        is TraktImportSuccess -> {
          uiState = TraktImportUiModel(isProgress = false)
          _messageLiveData.value = R.string.textTraktImportComplete
        }
        is TraktImportError -> {
          uiState = TraktImportUiModel(isProgress = false)
          _messageLiveData.value = R.string.textTraktImportError
        }
        is TraktImportAuthError -> {
          viewModelScope.launch {
            userManager.revokeToken()
            _errorLiveData.value = R.string.errorTraktAuthorization
            uiState = TraktImportUiModel(isProgress = false, authError = true)
          }
        }
      }
    }
  }
}
