package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.MovieCollection
import com.michaldrabik.data_local.sources.MovieCollectionsLocalDataSource

@Dao
interface MovieCollectionsDao : BaseDao<MovieCollection>, MovieCollectionsLocalDataSource {

  @Query("SELECT * FROM movies_collections WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): MovieCollection?

  @Query("SELECT * FROM movies_collections WHERE id_trakt_movie == :movieTraktId")
  override suspend fun getByMovieId(movieTraktId: Long): List<MovieCollection>

  @Transaction
  override suspend fun replaceByMovieId(
    movieTraktId: Long,
    entities: List<MovieCollection>,
  ) {
    val deleteCollections = getByMovieId(movieTraktId).map { it.idTrakt }

    deleteCollectionsItems(deleteCollections)
    deleteCollections(deleteCollections)

    insert(entities)
  }

  override suspend fun insertAll(items: List<MovieCollection>) {
    insert(items)
  }

  @Query("DELETE FROM movies_collections WHERE id_trakt IN (:collectionIds)")
  suspend fun deleteCollections(collectionIds: List<Long>)

  @Query("DELETE FROM movies_collections_items WHERE id_trakt_collection IN (:collectionIds)")
  suspend fun deleteCollectionsItems(collectionIds: List<Long>)
}
