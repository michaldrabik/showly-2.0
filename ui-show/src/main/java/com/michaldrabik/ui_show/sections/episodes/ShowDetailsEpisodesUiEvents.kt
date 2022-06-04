// ktlint-disable filename
package com.michaldrabik.ui_show.sections.episodes

import com.michaldrabik.ui_base.utilities.events.Event

sealed class ShowDetailsEpisodesEvent<T>(action: T) : Event<T>(action) {

  object Finish : ShowDetailsEpisodesEvent<Unit>(Unit)
}
