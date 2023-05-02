package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.MovieCollectionItem
import com.michaldrabik.data_local.sources.MovieCollectionsItemsLocalDataSource

@Dao
interface MovieCollectionsItemsDao : BaseDao<MovieCollectionItem>, MovieCollectionsItemsLocalDataSource {

  @Query("SELECT movies.*, movies_collections_items.created_at, movies_collections_items.updated_at FROM movies INNER JOIN movies_collections_items USING(id_trakt)")
  override suspend fun getById(collectionId: Long): List<Movie>

}
