package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows")
data class Show(
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_tvdb") var idTvdb: Long?,
  @ColumnInfo(name = "id_tmdb") var idTmdb: Long?,
  @ColumnInfo(name = "id_imdb") var idImdb: String?,
  @ColumnInfo(name = "id_slug") var idSlug: String?,
  @ColumnInfo(name = "title") var title: String?,
  @ColumnInfo(name = "year") var year: Int?,
  @ColumnInfo(name = "overview") var overview: String?
)