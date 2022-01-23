package com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation

data class ShowContextItem(
  val show: Show,
  val image: Image,
  val translation: Translation?,
  val userRating: Int?,
  val isMyShow: Boolean,
  val isWatchlist: Boolean,
  val isHidden: Boolean,
  val isPinnedTop: Boolean,
  val isOnHold: Boolean
) {

  fun isInCollection() = isHidden || isWatchlist || isMyShow
}
