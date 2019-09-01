package com.michaldrabik.showly2.model

data class Image(
  val idTvdb: Long,
  val type: ImageType,
  val fileUrl: String,
  val thumbnailUrl: String,
  val status: Status
) {

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

  companion object {
    fun createUnknown(type: ImageType) = Image(0, type, "", "", Status.UNKNOWN)

    fun createUnavailable(type: ImageType) = Image(0, type, "", "", Status.UNAVAILABLE)
  }
}