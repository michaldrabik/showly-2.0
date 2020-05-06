package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actors")
data class Actor(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
  @ColumnInfo(name = "id_tvdb") var idTvdb: Long,
  @ColumnInfo(name = "id_imdb") var idImdb: String?,
  @ColumnInfo(name = "id_tvdb_show") var idShowTvdb: Long,
  @ColumnInfo(name = "name") var name: String,
  @ColumnInfo(name = "role") var role: String,
  @ColumnInfo(name = "sortOrder") var sortOrder: Int,
  @ColumnInfo(name = "image") var image: String,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long

)
