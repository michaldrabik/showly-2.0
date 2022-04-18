package com.michaldrabik.showly2.ui.main

import com.michaldrabik.showly2.utilities.deeplink.DeepLinkBundle
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_settings.helpers.AppLanguage

// TODO Split events into their Channel
data class MainUiState(
  val isLoading: Boolean = false,
  val isInitialRun: Event<Boolean>? = null,
  val showWhatsNew: Event<Boolean>? = null,
  val initialLanguage: Event<AppLanguage>? = null,
  val showRateApp: Event<Boolean>? = null,
  val showMask: Boolean = false,
  val openLink: Event<DeepLinkBundle>? = null,
)
