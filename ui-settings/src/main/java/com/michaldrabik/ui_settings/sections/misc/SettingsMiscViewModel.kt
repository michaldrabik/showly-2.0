package com.michaldrabik.ui_settings.sections.misc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.sections.misc.cases.SettingsMiscCacheCase
import com.michaldrabik.ui_settings.sections.misc.cases.SettingsMiscUserCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsMiscViewModel @Inject constructor(
  private val userCase: SettingsMiscUserCase,
  private val cacheCase: SettingsMiscCacheCase,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val userIdState = MutableStateFlow("")
  private val loadingState = MutableStateFlow(false)

  fun loadSettings() {
    viewModelScope.launch {
      userIdState.value = userCase.getUserId()
    }
  }

  fun deleteImagesCache(context: Context) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) { Glide.get(context).clearDiskCache() }
      Glide.get(context).clearMemory()
      cacheCase.deleteImagesCache()
      messageChannel.send(MessageEvent.Info(R.string.textImagesCacheCleared))
    }
  }

  val uiState = combine(
    userIdState,
    loadingState
  ) { s1, _ ->
    SettingsMiscUiState(
      userId = s1,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SettingsMiscUiState()
  )
}
