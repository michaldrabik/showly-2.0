package com.michaldrabik.showly2.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.utilities.MessageEvent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
  private val interactor: SettingsInteractor
) : BaseViewModel<SettingsUiModel>() {

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  fun setRecentShowsAmount(amount: Int) {
    viewModelScope.launch {
      interactor.setRecentShowsAmount(amount)
      refreshSettings()
    }
  }

  fun enablePushNotifications(enable: Boolean) {
    viewModelScope.launch {
      interactor.enablePushNotifications(enable)
      refreshSettings()
    }
  }

  fun enableEpisodesAnnouncements(enable: Boolean, context: Context) {
    viewModelScope.launch {
      interactor.enableEpisodesAnnouncements(enable, context)
      refreshSettings()
    }
  }

  fun enableAnticipatedShows(enable: Boolean) {
    viewModelScope.launch {
      interactor.enableAnticipatedShows(enable)
      refreshSettings()
    }
  }

  fun enableMyShowsSection(section: MyShowsSection, isEnabled: Boolean) {
    viewModelScope.launch {
      interactor.enableMyShowsSection(section, isEnabled)
      refreshSettings()
    }
  }

  fun setWhenToNotify(delay: NotificationDelay, context: Context) {
    viewModelScope.launch {
      interactor.setWhenToNotify(delay, context)
      refreshSettings()
    }
  }

  fun authorizeTrakt(authData: Uri?) {
    if (authData == null) return
    viewModelScope.launch {
      try {
        interactor.authorizeTrakt(authData)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktLoginSuccess)
        refreshSettings()
      } catch (t: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorAuthorization)
      }
    }
  }

  fun logoutTrakt() {
    viewModelScope.launch {
      interactor.logoutTrakt()
      _messageLiveData.value = MessageEvent.info(R.string.textTraktLogoutSuccess)
      refreshSettings()
    }
  }

  fun deleteImagesCache(context: Context) {
    viewModelScope.launch {
      withContext(IO) { Glide.get(context).clearDiskCache() }
      Glide.get(context).clearMemory()
      interactor.deleteImagesCache()
      _messageLiveData.value = MessageEvent.info(R.string.textImagesCacheCleared)
    }
  }

  private suspend fun refreshSettings() {
    uiState = SettingsUiModel(
      settings = interactor.getSettings(),
      isSignedInTrakt = interactor.isTraktAuthorized(),
      traktUsername = interactor.getTraktUsername()
    )
  }
}
