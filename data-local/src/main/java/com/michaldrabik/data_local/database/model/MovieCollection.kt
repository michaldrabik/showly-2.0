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
  tableName = "movies_collections",
  indices = [
    Index(value = ["id_trakt"]),
    Index(value = ["id_trakt_movie"]),
  ],
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_movie"),
      onDelete = ForeignKey.CASCADE
    )
  ]
)
@TypeConverters(DateConverter::class)
data class MovieCollection(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_trakt_movie") val idTraktMovie: Long,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "description") val description: String,
  @ColumnInfo(name = "item_count") val itemCount: Int,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
