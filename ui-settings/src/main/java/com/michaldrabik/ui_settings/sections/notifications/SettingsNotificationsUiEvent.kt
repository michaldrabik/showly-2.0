// ktlint-disable filename
package com.michaldrabik.ui_settings.sections.notifications

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class SettingsNotificationsUiEvent<T>(action: T) : Event<T>(action) {
  data object RequestNotificationsPermission : SettingsNotificationsUiEvent<Unit>(Unit)
}
