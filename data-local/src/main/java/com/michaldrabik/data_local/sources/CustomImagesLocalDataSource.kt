package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.CustomImage

interface CustomImagesLocalDataSource {

  suspend fun getById(traktId: Long, family: String, type: String): CustomImage?

  suspend fun deleteById(traktId: Long, family: String, type: String)

  suspend fun insertImage(image: CustomImage)

  suspend fun upsert(image: CustomImage)

  suspend fun deleteAll()
}
