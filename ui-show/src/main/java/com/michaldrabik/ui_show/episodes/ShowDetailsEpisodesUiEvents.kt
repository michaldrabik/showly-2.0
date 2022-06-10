// ktlint-disable filename
package com.michaldrabik.ui_show.episodes

import androidx.annotation.IdRes
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season

sealed class ShowDetailsEpisodesEvent<T>(action: T) : Event<T>(action) {

  data class OpenEpisodeDetails(
    val bundle: EpisodeBundle,
    val isWatched: Boolean
  ) : ShowDetailsEpisodesEvent<EpisodeBundle>(bundle)

  data class OpenRateSeason(
    val season: Season,
  ) : ShowDetailsEpisodesEvent<Season>(season)

  data class RemoveFromTrakt(
    @IdRes val actionId: Int,
    val mode: RemoveTraktBottomSheet.Mode,
    val traktIds: List<IdTrakt>
  ) : ShowDetailsEpisodesEvent<Int>(actionId)

  object Finish : ShowDetailsEpisodesEvent<Unit>(Unit)
}
