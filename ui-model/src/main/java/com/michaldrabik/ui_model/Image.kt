package com.michaldrabik.ui_model

import com.michaldrabik.common.Config.AWS_IMAGE_BASE_URL
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_FANART_URL
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_POSTER_URL
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_PROFILE_URL
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_BANNERS_URL
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageSource.AWS
import com.michaldrabik.ui_model.ImageSource.CUSTOM
import com.michaldrabik.ui_model.ImageSource.TMDB
import com.michaldrabik.ui_model.ImageSource.TVDB
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNKNOWN
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.ImageType.PROFILE

data class Image(
  val id: Long,
  val idTvdb: IdTvdb,
  val idTmdb: IdTmdb,
  val type: ImageType,
  val family: ImageFamily,
  val fileUrl: String,
  val thumbnailUrl: String,
  val status: ImageStatus,
  val source: ImageSource
) {

  val fullFileUrl = when (source) {
    TVDB -> "$TVDB_IMAGE_BASE_BANNERS_URL$fileUrl"
    TMDB -> when (type) {
      POSTER -> "${TMDB_IMAGE_BASE_POSTER_URL}$fileUrl"
      FANART, FANART_WIDE -> "${TMDB_IMAGE_BASE_FANART_URL}$fileUrl"
      PROFILE -> "${TMDB_IMAGE_BASE_PROFILE_URL}$fileUrl"
      else -> ""
    }
    AWS -> "$AWS_IMAGE_BASE_URL$fileUrl"
    CUSTOM -> fileUrl
  }

  companion object {
    fun createUnknown(
      type: ImageType,
      family: ImageFamily = SHOW,
      source: ImageSource = TVDB
    ) = Image(0, IdTvdb(0), IdTmdb(0), type, family, "", "", UNKNOWN, source)

    fun createUnavailable(
      type: ImageType,
      family: ImageFamily = SHOW,
      source: ImageSource = TVDB
    ) = Image(0, IdTvdb(0), IdTmdb(0), type, family, "", "", UNAVAILABLE, source)

    fun createAvailable(
      ids: Ids,
      type: ImageType,
      family: ImageFamily,
      path: String,
      source: ImageSource
    ) = Image(0, ids.tvdb, ids.tmdb, type, family, path, "", AVAILABLE, source)
  }
}
