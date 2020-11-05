package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows_images")
data class Image(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_tvdb", defaultValue = "-1") var idTvdb: Long,
  @ColumnInfo(name = "type", defaultValue = "") var type: String,
  @ColumnInfo(name = "family", defaultValue = "") var family: String,
  @ColumnInfo(name = "file_url", defaultValue = "") var fileUrl: String,
  @ColumnInfo(name = "thumbnail_url", defaultValue = "") var thumbnailUrl: String,
  @ColumnInfo(name = "source", defaultValue = "tvdb") var source: String
)
