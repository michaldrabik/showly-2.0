package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_archive",
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
data class ArchiveShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long
) {

  companion object {
    fun fromTraktId(traktId: Long, createdAt: Long) =
      ArchiveShow(
        idTrakt = traktId,
        createdAt = createdAt,
        updatedAt = createdAt
      )
  }
}
