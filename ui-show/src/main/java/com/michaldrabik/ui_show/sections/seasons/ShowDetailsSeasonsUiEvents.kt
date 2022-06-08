// ktlint-disable filename
package com.michaldrabik.ui_show.sections.seasons

import androidx.annotation.IdRes
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.IdTrakt

sealed class ShowDetailsSeasonsEvent<T>(action: T) : Event<T>(action) {

  data class OpenSeasonEpisodes(
    val showId: IdTrakt,
    val seasonId: IdTrakt
  ) : ShowDetailsSeasonsEvent<IdTrakt>(showId)

  data class RemoveFromTrakt(
    @IdRes val actionId: Int,
    val mode: RemoveTraktBottomSheet.Mode,
    val traktIds: List<IdTrakt>
  ) : ShowDetailsSeasonsEvent<Int>(actionId)
}
