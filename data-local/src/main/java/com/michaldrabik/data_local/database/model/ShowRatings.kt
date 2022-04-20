package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_ratings",
  indices = [Index(value = ["id_trakt"], unique = true)],
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt"),
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class ShowRatings(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "trakt") val trakt: String?,
  @ColumnInfo(name = "imdb") val imdb: String?,
  @ColumnInfo(name = "metascore") val metascore: String?,
  @ColumnInfo(name = "rotten_tomatoes") val rottenTomatoes: String?,
  @ColumnInfo(name = "rotten_tomatoes_url") val rottenTomatoesUrl: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
