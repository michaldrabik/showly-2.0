package com.michaldrabik.ui_settings

import com.michaldrabik.ui_model.Settings

data class SettingsUiState(
  val settings: Settings? = null,
  val isPremium: Boolean = false,
  val userId: String = "",
)
