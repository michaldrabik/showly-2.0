package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.model.ImageFamily.SHOW

data class Image(
  val id: Long,
  val idTvdb: IdTvdb,
  val type: ImageType,
  val family: ImageFamily,
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
    fun createUnknown(type: ImageType, family: ImageFamily = SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", Status.UNKNOWN)

    fun createUnavailable(type: ImageType, family: ImageFamily = SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", Status.UNAVAILABLE)
  }
}