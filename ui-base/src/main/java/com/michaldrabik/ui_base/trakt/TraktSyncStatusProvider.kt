package com.michaldrabik.ui_base.trakt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktSyncStatusProvider @Inject constructor() {

  private val _status = MutableStateFlow(false)
  val status = _status.asStateFlow()

  fun setSyncing(value: Boolean) {
    _status.value = value
    Timber.d("Syncing status changed: $value")
  }
}
