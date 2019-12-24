package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.storage.database.model.Image as ImageDb
import java.util.Locale.ROOT
import javax.inject.Inject

class ImageMapper @Inject constructor() {

  fun fromDatabase(imageDb: ImageDb): Image {
    return Image(
      imageDb.id,
      IdTvdb(imageDb.idTvdb),
      enumValueOf(imageDb.type.toUpperCase(ROOT)),
      enumValueOf(imageDb.family.toUpperCase(ROOT)),
      imageDb.fileUrl,
      imageDb.thumbnailUrl,
      Image.Status.AVAILABLE
    )
  }

  fun toDatabase(image: Image): ImageDb =
    ImageDb(
      idTvdb = image.idTvdb.id,
      type = image.type.key,
      family = image.family.key,
      fileUrl = image.fileUrl,
      thumbnailUrl = image.thumbnailUrl
    )
}
