
@file:Suppress("ktlint:standard:filename")

package com.michaldrabik.ui_lists.details

import com.michaldrabik.ui_base.utilities.events.Event

sealed class ListDetailsUiEvent<T>(action: T) : Event<T>(action)
