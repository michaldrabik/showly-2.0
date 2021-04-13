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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "family") var family: String,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "file_url") var fileUrl: String
)
