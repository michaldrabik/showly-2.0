package com.michaldrabik.ui_settings.sections.trakt

import com.michaldrabik.ui_model.Settings

data class SettingsTraktUiState(
  val settings: Settings? = null,
  val isSignedInTrakt: Boolean = false,
  val isSigningIn: Boolean = false,
  val traktUsername: String = "",
  val isPremium: Boolean = false,
)
