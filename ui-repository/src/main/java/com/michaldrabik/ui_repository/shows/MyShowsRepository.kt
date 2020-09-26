package com.michaldrabik.ui_repository.shows

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
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

  suspend fun loadAll(ids: List<IdTrakt>) =
    database.myShowsDao().getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    database.myShowsDao().getAllRecent(amount)
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = database.myShowsDao().getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbShow = MyShow.fromTraktId(id.id, nowUtcMillis(), 0)
    database.myShowsDao().insert(listOf(dbShow))
  }

  suspend fun delete(id: IdTrakt) = database.myShowsDao().deleteById(id.id)
}
