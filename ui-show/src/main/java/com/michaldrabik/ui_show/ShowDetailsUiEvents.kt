package com.michaldrabik.ui_show

import androidx.annotation.IdRes
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_show.seasons.SeasonListItem

data class FinishUiEvent(val isSuccess: Boolean) : Event<Boolean>(isSuccess)

data class RemoveTraktUiEvent(
  @IdRes val actionId: Int,
  val mode: RemoveTraktBottomSheet.Mode,
  val traktIds: List<IdTrakt>
) : Event<Int>(actionId)

data class SeasonTranslationUiEvent(val item: SeasonListItem) : Event<SeasonListItem>(item)
