package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "custom_images",
  indices = [
    Index(value = ["id_trakt", "family", "type"])
  ]
)
data class CustomImage(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "family") val family: String,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "file_url") val fileUrl: String
)
