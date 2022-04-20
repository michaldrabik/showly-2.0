package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
  tableName = "movies_related",
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_related_movie"),
      onDelete = CASCADE
    )
  ]
)
data class RelatedMovie(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt", defaultValue = "-1") val idTrakt: Long,
  @ColumnInfo(name = "id_trakt_related_movie", defaultValue = "-1", index = true) val idTraktRelatedMovie: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long
) {

  companion object {
    fun fromTraktId(traktId: Long, relatedTraktId: Long, nowUtcMillis: Long) =
      RelatedMovie(
        idTrakt = traktId,
        idTraktRelatedMovie = relatedTraktId,
        updatedAt = nowUtcMillis
      )
  }
}
