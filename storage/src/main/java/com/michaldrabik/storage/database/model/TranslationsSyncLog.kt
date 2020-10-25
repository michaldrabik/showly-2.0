package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_translations_log")
data class TranslationsSyncLog(
  @PrimaryKey @ColumnInfo(name = "id_show_trakt", defaultValue = "-1") var idTrakt: Long,
  @ColumnInfo(name = "synced_at", defaultValue = "0") var syncedAt: Long
)
