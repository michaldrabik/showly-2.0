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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
  @ColumnInfo(name = "id_tmdb_person") var idTmdbPerson: Long,
  @ColumnInfo(name = "mode") var mode: String,
  @ColumnInfo(name = "department") var department: String,
  @ColumnInfo(name = "character") var character: String?,
  @ColumnInfo(name = "job") var job: String?,
  @ColumnInfo(name = "episodes_count") var episodesCount: Int,
  @ColumnInfo(name = "id_trakt_show") var idTraktShow: Long?,
  @ColumnInfo(name = "id_trakt_movie") var idTraktMovie: Long?,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime
)
