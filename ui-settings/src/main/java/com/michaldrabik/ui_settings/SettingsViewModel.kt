package com.michaldrabik.ui_settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.cases.SettingsMainCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val mainCase: SettingsMainCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val settingsState = MutableStateFlow<Settings?>(null)
  private val premiumState = MutableStateFlow(false)
  private val userIdState = MutableStateFlow("")

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  private suspend fun refreshSettings() {
    settingsState.value = mainCase.getSettings()
    premiumState.value = mainCase.isPremium()
    userIdState.value = mainCase.getUserId()
  }

  fun deleteImagesCache(context: Context) {
    viewModelScope.launch {
      withContext(IO) { Glide.get(context).clearDiskCache() }
      Glide.get(context).clearMemory()
      mainCase.deleteImagesCache()
      messageChannel.send(MessageEvent.Info(R.string.textImagesCacheCleared))
    }
  }

  val uiState = combine(
    settingsState,
    premiumState,
    userIdState
  ) { s1, s2, s3 ->
    SettingsUiState(
      settings = s1,
      isPremium = s2,
      userId = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsUiState()
  )
}
