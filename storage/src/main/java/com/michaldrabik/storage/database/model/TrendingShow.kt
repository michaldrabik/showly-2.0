package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows_trending")
data class TrendingShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long
)