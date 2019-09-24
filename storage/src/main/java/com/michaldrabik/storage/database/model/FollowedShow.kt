package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows_followed")
data class FollowedShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt", defaultValue = "-1") var idTrakt: Long,
  @ColumnInfo(name = "created_at", defaultValue = "-1") var createdAt: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") var updatedAt: Long
) {

  companion object {
    fun fromTraktId(traktId: Long): FollowedShow {
      val timestamp = System.currentTimeMillis()
      return FollowedShow(idTrakt = traktId, createdAt = timestamp, updatedAt = timestamp)
    }
  }
}