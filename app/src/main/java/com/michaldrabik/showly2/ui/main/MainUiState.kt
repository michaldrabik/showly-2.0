package com.michaldrabik.showly2.ui.main

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_settings.helpers.AppLanguage

data class MainUiState(
  val isInitialRun: Event<Boolean>? = null,
  val showWhatsNew: Event<Boolean>? = null,
  val initialLanguage: Event<AppLanguage>? = null,
  val showRateApp: Event<Boolean>? = null,
)
