package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "people",
  indices = [
    Index(value = ["id_trakt"]),
    Index(value = ["id_tmdb"], unique = true),
  ]
)
@TypeConverters(DateConverter::class)
data class Person(
  @PrimaryKey @ColumnInfo(name = "id_tmdb") var idTmdb: Long,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long?,
  @ColumnInfo(name = "id_imdb") var idImdb: String?,
  @ColumnInfo(name = "name") var name: String,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "biography") var biography: String?,
  @ColumnInfo(name = "birthday") var birthday: String?,
  @ColumnInfo(name = "birthplace") var birthplace: String?,
  @ColumnInfo(name = "character") var character: String?,
  @ColumnInfo(name = "deathday") var deathday: String?,
  @ColumnInfo(name = "image_path") var image: String?,
  @ColumnInfo(name = "homepage") var homepage: String?,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime
)
