package com.michaldrabik.showly2.model

data class ImageUrl(
  val url: String,
  val status: Status
) {

  companion object {
    fun fromString(urlString: String?) = when {
      urlString == null -> ImageUrl("", Status.UNAVAILABLE)
      urlString.isEmpty() -> ImageUrl("", Status.UNKNOWN)
      else -> ImageUrl(urlString, Status.AVAILABLE)
    }
  }

  /**
   * AVAILABLE - image's web url is known to be valid when used the last time.
   * UNKNOWN - image's web url has not been yet checked with remote images service (TVDB).
   * UNAVAILABLE - remote images service does not contain any valid image url.
   */
  enum class Status {
    AVAILABLE,
    UNKNOWN,
    UNAVAILABLE
  }
}