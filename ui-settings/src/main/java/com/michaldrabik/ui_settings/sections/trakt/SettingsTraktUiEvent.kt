package com.michaldrabik.ui_settings.sections.trakt

import com.michaldrabik.ui_base.utilities.events.Event

sealed class SettingsTraktUiEvent<T>(action: T) : Event<T>(action) {
  data object StartAuthorization : SettingsTraktUiEvent<Unit>(Unit)
  data object RequestNotificationsPermission : SettingsTraktUiEvent<Unit>(Unit)
}
