package com.michaldrabik.ui_my_shows.main

import com.michaldrabik.ui_base.utilities.events.Event

sealed class FollowedShowsUiEvent<T>(action: T) : Event<T>(action) {
  object OpenPremium : FollowedShowsUiEvent<Unit>(Unit)
}
