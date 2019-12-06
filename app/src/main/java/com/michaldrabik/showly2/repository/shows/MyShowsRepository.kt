package com.michaldrabik.showly2.repository.shows

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.MyShow
import javax.inject.Inject

@AppScope
class MyShowsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun load(id: IdTrakt) =
    database.myShowsDao().getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAll() =
    database.myShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    database.myShowsDao().getAllRecent()
      .take(amount)
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = database.myShowsDao().getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbShow = MyShow.fromTraktId(id.id, nowUtcMillis())
    database.myShowsDao().insert(listOf(dbShow))
  }

  suspend fun delete(id: IdTrakt) = database.myShowsDao().deleteById(id.id)
}