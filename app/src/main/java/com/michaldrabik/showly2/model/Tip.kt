package com.michaldrabik.showly2.model

import androidx.annotation.StringRes
import com.michaldrabik.showly2.R

enum class Tip(@StringRes val textResId: Int) {
  MENU_DISCOVER(R.string.textTipDiscover),
  MENU_MY_SHOWS(R.string.textTipMyShows),
  SHOW_DETAILS_GALLERY(R.string.textTipShowDetailsGallery)
}
