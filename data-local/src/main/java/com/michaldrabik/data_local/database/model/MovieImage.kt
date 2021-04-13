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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") var idTmdb: Long,
  @ColumnInfo(name = "type", defaultValue = "") var type: String,
  @ColumnInfo(name = "file_url", defaultValue = "") var fileUrl: String,
  @ColumnInfo(name = "source", defaultValue = "tmdb") var source: String
)
