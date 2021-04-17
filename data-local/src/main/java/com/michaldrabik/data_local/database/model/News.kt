package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "news"
)
data class News(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_news") var idNews: String,
  @ColumnInfo(name = "title") var title: String,
  @ColumnInfo(name = "url") var url: String,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "image") var image: String?,
  @ColumnInfo(name = "score") var score: Long,
  @ColumnInfo(name = "dated_at") var datedAt: Long,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long,
)
