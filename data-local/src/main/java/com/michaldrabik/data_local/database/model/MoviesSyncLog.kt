package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_movies_log")
data class MoviesSyncLog(
  @PrimaryKey @ColumnInfo(name = "id_movie_trakt", defaultValue = "-1") var idTrakt: Long,
  @ColumnInfo(name = "synced_at", defaultValue = "0") var syncedAt: Long
)
