package com.michaldrabik.ui_model

import androidx.annotation.StringRes

enum class Tip(
  @StringRes val textResId: Int,
) {
  MENU_MODES(R.string.textTipBottomModeMenu),
  MENU_DISCOVER(R.string.textTipDiscover),
  MENU_MY_SHOWS(R.string.textTipMyShows),
  SHOW_DETAILS_GALLERY(R.string.textTipShowDetailsGallery),
  PERSON_DETAILS_GALLERY(R.string.textTipShowDetailsGallery),
  WATCHLIST_ITEM_PIN(R.string.textTipWatchlistPinItem),
  LIST_ITEM_SWIPE_DELETE(R.string.textTipListSwipeToDelete)
}
