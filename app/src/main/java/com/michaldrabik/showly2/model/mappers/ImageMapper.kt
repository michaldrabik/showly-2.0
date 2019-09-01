package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Image
import java.util.Locale.ROOT
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Image as ImageDb

class ImageMapper @Inject constructor() {

  fun fromDb(imageDb: ImageDb): Image {
    return Image(
      imageDb.id,
      imageDb.idTvdb,
      enumValueOf(imageDb.type.toUpperCase(ROOT)),
      imageDb.fileUrl,
      imageDb.thumbnailUrl,
      Image.Status.AVAILABLE
    )
  }

  fun toDb(image: Image): ImageDb =
    ImageDb(
      idTvdb = image.idTvdb,
      type = image.type.key,
      fileUrl = image.fileUrl,
      thumbnailUrl = image.thumbnailUrl
    )
}