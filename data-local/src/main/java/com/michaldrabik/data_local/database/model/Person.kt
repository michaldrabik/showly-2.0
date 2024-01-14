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
  @PrimaryKey @ColumnInfo(name = "id_tmdb") val idTmdb: Long,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long?,
  @ColumnInfo(name = "id_imdb") val idImdb: String?,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "department") val department: String,
  @ColumnInfo(name = "biography") val biography: String?,
  @ColumnInfo(name = "biography_translation") val biographyTranslation: String?,
  @ColumnInfo(name = "birthday") val birthday: String?,
  @ColumnInfo(name = "birthplace") val birthplace: String?,
  @ColumnInfo(name = "character") val character: String?,
  @ColumnInfo(name = "episodes_count") val episodesCount: Int?,
  @ColumnInfo(name = "job") val job: String?,
  @ColumnInfo(name = "deathday") val deathday: String?,
  @ColumnInfo(name = "image_path") val image: String?,
  @ColumnInfo(name = "homepage") val homepage: String?,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
  @ColumnInfo(name = "details_updated_at") val detailsUpdatedAt: ZonedDateTime?,
)
