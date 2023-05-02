package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Movie

interface MovieCollectionsItemsLocalDataSource {

  suspend fun getById(collectionId: Long): List<Movie>

}
