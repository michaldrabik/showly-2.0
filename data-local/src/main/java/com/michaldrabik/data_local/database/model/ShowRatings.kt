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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "trakt") var trakt: String?,
  @ColumnInfo(name = "imdb") var imdb: String?,
  @ColumnInfo(name = "metascore") var metascore: String?,
  @ColumnInfo(name = "rotten_tomatoes") var rottenTomatoes: String?,
  @ColumnInfo(name = "rotten_tomatoes_url") var rottenTomatoesUrl: String?,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long,
)
