package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "movies_images",
  indices = [
    Index(value = ["id_tmdb", "type"])
  ]
)
data class MovieImage(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") val idTmdb: Long,
  @ColumnInfo(name = "type", defaultValue = "") val type: String,
  @ColumnInfo(name = "file_url", defaultValue = "") val fileUrl: String,
  @ColumnInfo(name = "source", defaultValue = "tmdb") val source: String
)
