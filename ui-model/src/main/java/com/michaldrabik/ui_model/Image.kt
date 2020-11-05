package com.michaldrabik.ui_model

import com.michaldrabik.common.Config.AWS_IMAGE_BASE_URL
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_BANNERS_URL
import com.michaldrabik.ui_model.ImageSource.AWS
import com.michaldrabik.ui_model.ImageSource.TVDB

data class Image(
  val id: Long,
  val idTvdb: IdTvdb,
  val type: ImageType,
  val family: ImageFamily,
  val fileUrl: String,
  val thumbnailUrl: String,
  val status: Status,
  val source: ImageSource
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

  val fullFileUrl = when (source) {
    TVDB -> "$TVDB_IMAGE_BASE_BANNERS_URL$fileUrl"
    AWS -> "$AWS_IMAGE_BASE_URL$fileUrl"
  }

  companion object {
    fun createUnknown(type: ImageType, family: ImageFamily = ImageFamily.SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", Status.UNKNOWN, TVDB)

    fun createUnavailable(type: ImageType, family: ImageFamily = ImageFamily.SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", Status.UNAVAILABLE, TVDB)
  }
}
