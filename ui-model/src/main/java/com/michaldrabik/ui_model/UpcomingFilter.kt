package com.michaldrabik.ui_model

enum class UpcomingFilter {
  OFF,
  UPCOMING,
  RELEASED,
  ;

  fun isActive() = this != OFF
}
