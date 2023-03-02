package com.michaldrabik.ui_my_movies.main

import com.michaldrabik.ui_base.utilities.events.Event

sealed class FollowedMoviesUiEvent<T>(action: T) : Event<T>(action) {
  object OpenPremium : FollowedMoviesUiEvent<Unit>(Unit)
}
