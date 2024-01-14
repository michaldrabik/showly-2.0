package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "sync_trakt_log",
  indices = [
    Index(value = ["id_trakt", "type"], unique = true)
  ]
)
data class TraktSyncLog(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt", index = true) val idTrakt: Long,
  @ColumnInfo(name = "type", index = true) val type: String,
  @ColumnInfo(name = "synced_at") val syncedAt: Long
)
