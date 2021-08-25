package com.michaldrabik.showly2.ui.main

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_settings.helpers.AppLanguage

data class MainUiState(
  val isInitialRun: ActionEvent<Boolean>? = null,
  val showWhatsNew: ActionEvent<Boolean>? = null,
  val initialLanguage: ActionEvent<AppLanguage>? = null,
  val showRateApp: ActionEvent<Boolean>? = null,
)
