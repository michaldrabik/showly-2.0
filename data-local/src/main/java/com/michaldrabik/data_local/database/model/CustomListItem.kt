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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_list") var idList: Long,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "rank") var rank: Long,
  @ColumnInfo(name = "listed_at") var listedAt: Long,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
)
