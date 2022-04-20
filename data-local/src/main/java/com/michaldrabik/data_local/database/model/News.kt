package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "news"
)
data class News(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_news") val idNews: String,
  @ColumnInfo(name = "title") val title: String,
  @ColumnInfo(name = "url") val url: String,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "image") val image: String?,
  @ColumnInfo(name = "score") val score: Long,
  @ColumnInfo(name = "dated_at") val datedAt: Long,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
