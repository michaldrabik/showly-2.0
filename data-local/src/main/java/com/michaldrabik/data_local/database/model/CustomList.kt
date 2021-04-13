package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "custom_lists",
  indices = [
    Index(value = ["id_trakt"], unique = true)
  ]
)
data class CustomList(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long?,
  @ColumnInfo(name = "id_slug") var idSlug: String,
  @ColumnInfo(name = "name") var name: String,
  @ColumnInfo(name = "description") var description: String?,
  @ColumnInfo(name = "privacy") var privacy: String,
  @ColumnInfo(name = "display_numbers") var displayNumbers: Boolean,
  @ColumnInfo(name = "allow_comments") var allowComments: Boolean,
  @ColumnInfo(name = "sort_by") var sortBy: String,
  @ColumnInfo(name = "sort_how") var sortHow: String,
  @ColumnInfo(name = "sort_by_local") var sortByLocal: String,
  @ColumnInfo(name = "sort_how_local") var sortHowLocal: String,
  @ColumnInfo(name = "filter_type_local") var filterTypeLocal: String,
  @ColumnInfo(name = "item_count") var itemCount: Long,
  @ColumnInfo(name = "comment_count") var commentCount: Long,
  @ColumnInfo(name = "likes") var likes: Long,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
)
