package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_related",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_related_show"),
      onDelete = CASCADE
    )
  ]
)
data class RelatedShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt", defaultValue = "-1") var idTrakt: Long,
  @ColumnInfo(name = "id_trakt_related_show", defaultValue = "-1", index = true) var idTraktRelatedShow: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") var updatedAt: Long
) {

  companion object {
    fun fromTraktId(traktId: Long, relatedShowTraktId: Long, nowUtcMillis: Long): RelatedShow {
      return RelatedShow(
        idTrakt = traktId,
        idTraktRelatedShow = relatedShowTraktId,
        updatedAt = nowUtcMillis
      )
    }
  }
}
