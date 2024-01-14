package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "people_shows_movies",
  foreignKeys = [
    ForeignKey(
      entity = Person::class,
      parentColumns = arrayOf("id_tmdb"),
      childColumns = arrayOf("id_tmdb_person"),
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["id_tmdb_person"]),
    Index(value = ["id_trakt_show", "mode"]),
    Index(value = ["id_trakt_movie", "mode"])
  ]
)
@TypeConverters(DateConverter::class)
data class PersonShowMovie(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
  @ColumnInfo(name = "id_tmdb_person") val idTmdbPerson: Long,
  @ColumnInfo(name = "mode") val mode: String,
  @ColumnInfo(name = "department") val department: String,
  @ColumnInfo(name = "character") val character: String?,
  @ColumnInfo(name = "job") val job: String?,
  @ColumnInfo(name = "episodes_count") val episodesCount: Int,
  @ColumnInfo(name = "id_trakt_show") val idTraktShow: Long?,
  @ColumnInfo(name = "id_trakt_movie") val idTraktMovie: Long?,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime
)
