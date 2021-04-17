package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
  @PrimaryKey @ColumnInfo(name = "id") var id: Long = 1,
  @ColumnInfo(name = "tvdb_token", defaultValue = "") var tvdbToken: String,
  @ColumnInfo(name = "tvdb_token_timestamp", defaultValue = "0") var tvdbTokenTimestamp: Long,
  @ColumnInfo(name = "trakt_token", defaultValue = "") var traktToken: String,
  @ColumnInfo(name = "trakt_refresh_token", defaultValue = "") var traktRefreshToken: String,
  @ColumnInfo(name = "trakt_token_timestamp", defaultValue = "0") var traktTokenTimestamp: Long,
  @ColumnInfo(name = "trakt_username", defaultValue = "") var traktUsername: String,
  @ColumnInfo(name = "reddit_token", defaultValue = "") var redditToken: String,
  @ColumnInfo(name = "reddit_token_timestamp", defaultValue = "0") var redditTokenTimestamp: Long,
)
