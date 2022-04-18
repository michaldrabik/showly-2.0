// ktlint-disable filename
package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.EpisodeBundle

data class EpisodeCheckActionUiEvent(
  val episode: EpisodeBundle,
  val isQuickRate: Boolean,
) : Event<EpisodeBundle>(episode)
