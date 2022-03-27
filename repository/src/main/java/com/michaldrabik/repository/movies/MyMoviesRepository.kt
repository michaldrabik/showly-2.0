package com.michaldrabik.repository.movies

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.MyMovie
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject

class MyMoviesRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun load(id: IdTrakt) =
    localSource.myMovies.getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun loadAll() =
    localSource.myMovies.getAll()
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAll(ids: List<IdTrakt>) =
    localSource.myMovies.getAll(ids.map { it.id })
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    localSource.myMovies.getAllRecent(amount)
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllIds() = localSource.myMovies.getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val movie = MyMovie.fromTraktId(id.id, nowUtcMillis())
    transactions.withTransaction {
      with(localSource) {
        myMovies.insert(listOf(movie))
        watchlistMovies.deleteById(id.id)
        archiveMovies.deleteById(id.id)
      }
    }
  }

  suspend fun delete(id: IdTrakt) =
    localSource.myMovies.deleteById(id.id)

  suspend fun exists(id: IdTrakt) =
    localSource.myMovies.checkExists(id.id)
}
