package com.michaldrabik.ui_settings.sections.spoilers

data class SettingsSpoilersUiState(
  val hasShowsSettingActive: Boolean = false,
  val hasMoviesSettingActive: Boolean = false,
  val hasEpisodesSettingActive: Boolean = false,
  val isTapToReveal: Boolean = false,
)
