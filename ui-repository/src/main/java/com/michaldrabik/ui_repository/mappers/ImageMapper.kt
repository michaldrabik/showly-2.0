package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.MovieImage
import java.util.Locale.ROOT
import javax.inject.Inject
import com.michaldrabik.storage.database.model.MovieImage as MovieImageDb
import com.michaldrabik.storage.database.model.ShowImage as ShowImageDb

class ImageMapper @Inject constructor() {

  fun fromDatabase(imageDb: ShowImageDb): Image {
    return Image(
      imageDb.id,
      IdTvdb(imageDb.idTvdb),
      enumValueOf(imageDb.type.toUpperCase(ROOT)),
      enumValueOf(imageDb.family.toUpperCase(ROOT)),
      imageDb.fileUrl,
      imageDb.thumbnailUrl,
      AVAILABLE,
      ImageSource.fromKey(imageDb.source)
    )
  }

  fun fromDatabase(imageDb: MovieImageDb): MovieImage {
    return MovieImage(
      imageDb.id,
      IdTmdb(imageDb.idTmdb),
      enumValueOf(imageDb.type.toUpperCase(ROOT)),
      imageDb.fileUrl,
      AVAILABLE,
      ImageSource.fromKey(imageDb.source)
    )
  }

  fun toDatabase(image: Image): ShowImageDb =
    ShowImageDb(
      idTvdb = image.idTvdb.id,
      type = image.type.key,
      family = image.family.key,
      fileUrl = image.fileUrl,
      thumbnailUrl = image.thumbnailUrl,
      source = image.source.key
    )

  fun toDatabase(image: MovieImage): MovieImageDb =
    MovieImageDb(
      idTmdb = image.idTmdb.id,
      type = image.type.key,
      fileUrl = image.fileUrl,
      source = image.source.key
    )
}
