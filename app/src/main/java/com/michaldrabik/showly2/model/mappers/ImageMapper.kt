package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Image
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Image as ImageDb

class ImageMapper @Inject constructor() {

  fun fromDb(imageDb: ImageDb): Image {
    return Image(
      imageDb.id,
      imageDb.idTvdb,
      enumValueOf(imageDb.type),
      imageDb.fileUrl,
      imageDb.thumbnailUrl,
      Image.Status.AVAILABLE
    )
  }

  fun toDb(image: Image): ImageDb =
    ImageDb(0, image.idTvdb, image.type.key, image.fileUrl, image.thumbnailUrl)

}