package com.michaldrabik.showly2.ui.shows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ShowDetailsInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadShowDetails(id: Long): Show {
    val show = database.showsDao().getById(id)
    if (show == null) {
      //TODO Fetch show info
    }
    return mappers.show.fromDatabase(show!!)
  }

  suspend fun loadBackgroundImage(show: Show): Image? {
    val image = database.imagesDao().getById(show.ids.tvdb, ImageType.FANART.key) ?: return null
    return mappers.image.fromDatabase(image)
  }

}