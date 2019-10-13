package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
  @PrimaryKey @ColumnInfo(name = "id") var id: Long = 1,
  @ColumnInfo(name = "is_initial_run", defaultValue = "0") var isInitialRun: Boolean,
  @ColumnInfo(name = "my_shows_running_sort_by", defaultValue = "NAME") var myShowsRunningSortBy: String,
  @ColumnInfo(name = "my_shows_incoming_sort_by", defaultValue = "NAME") var myShowsIncomingSortBy: String,
  @ColumnInfo(name = "my_shows_ended_sort_by", defaultValue = "NAME") var myShowsEndedSortBy: String
)