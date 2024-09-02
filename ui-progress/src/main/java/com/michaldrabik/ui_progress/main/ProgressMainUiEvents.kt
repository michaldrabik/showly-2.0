
@file:Suppress("ktlint:standard:filename")

package com.michaldrabik.ui_progress.main

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.ProgressDateSelectionType
import com.michaldrabik.ui_model.Show

data class EpisodeCheckActionUiEvent(
  val episode: EpisodeBundle,
  val dateSelectionType: ProgressDateSelectionType,
) : Event<EpisodeBundle>(episode)

data class OpenEpisodeDetails(
  val show: Show,
  val episode: Episode,
  val isWatched: Boolean,
) : Event<Episode>(episode)

object RequestWidgetsUpdate : Event<Unit>(Unit)
