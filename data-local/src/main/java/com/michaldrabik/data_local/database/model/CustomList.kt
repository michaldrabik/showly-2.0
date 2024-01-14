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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long?,
  @ColumnInfo(name = "id_slug") val idSlug: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "description") val description: String?,
  @ColumnInfo(name = "privacy") val privacy: String,
  @ColumnInfo(name = "display_numbers") val displayNumbers: Boolean,
  @ColumnInfo(name = "allow_comments") val allowComments: Boolean,
  @ColumnInfo(name = "sort_by") val sortBy: String,
  @ColumnInfo(name = "sort_how") val sortHow: String,
  @ColumnInfo(name = "sort_by_local") val sortByLocal: String,
  @ColumnInfo(name = "sort_how_local") val sortHowLocal: String,
  @ColumnInfo(name = "filter_type_local") val filterTypeLocal: String,
  @ColumnInfo(name = "item_count") val itemCount: Long,
  @ColumnInfo(name = "comment_count") val commentCount: Long,
  @ColumnInfo(name = "likes") val likes: Long,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long
)
