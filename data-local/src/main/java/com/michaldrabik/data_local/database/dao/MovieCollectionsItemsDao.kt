// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.MovieCollectionItem
import com.michaldrabik.data_local.sources.MovieCollectionsItemsLocalDataSource

@Dao
interface MovieCollectionsItemsDao : BaseDao<MovieCollectionItem>, MovieCollectionsItemsLocalDataSource {

  @Query("SELECT movies.*, movies_collections_items.created_at, movies_collections_items.updated_at FROM movies INNER JOIN movies_collections_items USING(id_trakt) WHERE id_trakt_collection == :collectionId ORDER BY rank ASC")
  override suspend fun getById(collectionId: Long): List<Movie>

  @Transaction
  override suspend fun replace(
    collectionId: Long,
    items: List<MovieCollectionItem>,
  ) {
    deleteById(collectionId)
    insert(items)
  }

  @Query("DELETE FROM movies_collections_items WHERE id_trakt_collection == :collectionId")
  override suspend fun deleteById(collectionId: Long)
}
