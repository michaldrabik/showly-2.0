package com.michaldrabik.ui_trakt_sync

import com.michaldrabik.ui_base.utilities.events.Event

sealed class TraktSyncUiEvent<T>(action: T) : Event<T>(action) {
  data object StartAuthorization : TraktSyncUiEvent<Unit>(Unit)
  data object RequestNotificationsPermission : TraktSyncUiEvent<Unit>(Unit)
  data object Finish : TraktSyncUiEvent<Unit>(Unit)
}
