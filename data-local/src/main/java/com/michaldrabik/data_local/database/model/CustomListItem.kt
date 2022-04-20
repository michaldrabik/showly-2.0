package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "custom_list_item",
  indices = [
    Index(value = ["id_list"], unique = false),
    Index(value = ["id_trakt", "type"], unique = false),
    Index(value = ["id_list", "id_trakt", "type"], unique = true)
  ],
  foreignKeys = [
    ForeignKey(
      entity = CustomList::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("id_list"),
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class CustomListItem(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_list") val idList: Long,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "rank") val rank: Long,
  @ColumnInfo(name = "listed_at") val listedAt: Long,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long
)
