package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_translations_log")
data class TranslationsSyncLog(
  @PrimaryKey @ColumnInfo(name = "id_show_trakt") val idTrakt: Long,
  @ColumnInfo(name = "synced_at") val syncedAt: Long
)
