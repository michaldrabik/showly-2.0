package com.michaldrabik.ui_model

import androidx.annotation.StringRes

data class StreamingService(
  val imagePath: String,
  val name: String,
  val options: List<Option>,
  val mediaName: String,
  val countryCode: String,
  val link: String,
) {

  enum class Option(@StringRes val resId: Int) {
    FLATRATE(R.string.textStreamingStream),
    BUY(R.string.textStreamingBuy),
    RENT(R.string.textStreamingRent),
    ADS(R.string.textStreamingAds),
    FREE(R.string.textStreamingFree)
  }
}
