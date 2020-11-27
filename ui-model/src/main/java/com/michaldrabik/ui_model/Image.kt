package com.michaldrabik.ui_model

import com.michaldrabik.common.Config.AWS_IMAGE_BASE_URL
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_BANNERS_URL
import com.michaldrabik.ui_model.ImageSource.AWS
import com.michaldrabik.ui_model.ImageSource.TVDB
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNKNOWN

data class Image(
  val id: Long,
  val idTvdb: IdTvdb,
  val type: ImageType,
  val family: ImageFamily,
  val fileUrl: String,
  val thumbnailUrl: String,
  val status: ImageStatus,
  val source: ImageSource
) {

  val fullFileUrl = when (source) {
    TVDB -> "$TVDB_IMAGE_BASE_BANNERS_URL$fileUrl"
    AWS -> "$AWS_IMAGE_BASE_URL$fileUrl"
    else -> ""
  }

  companion object {
    fun createUnknown(type: ImageType, family: ImageFamily = ImageFamily.SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", UNKNOWN, TVDB)

    fun createUnavailable(type: ImageType, family: ImageFamily = ImageFamily.SHOW) =
      Image(0, IdTvdb(0), type, family, "", "", UNAVAILABLE, TVDB)
  }
}
