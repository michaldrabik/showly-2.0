package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
  @PrimaryKey @ColumnInfo(name = "id") var id: Long = 1,
  @ColumnInfo(name = "tvdb_token", defaultValue = "") var tvdbToken: String,
  @ColumnInfo(name = "tvdb_token_timestamp", defaultValue = "0") var tvdbTokenTimestamp: Long
)