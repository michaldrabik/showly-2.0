package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "custom_lists_items",
  indices = [
    Index(value = ["id_trakt", "type"], unique = false),
    Index(value = ["id_list"], unique = false)
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
data class CustomListItems(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_list") var idList: Long,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "rank") var rank: Long,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
)
