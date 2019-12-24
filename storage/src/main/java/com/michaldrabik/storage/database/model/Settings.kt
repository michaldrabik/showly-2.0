package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
  @PrimaryKey @ColumnInfo(name = "id") var id: Long = 1,
  @ColumnInfo(name = "is_initial_run", defaultValue = "0") var isInitialRun: Boolean,
  @ColumnInfo(name = "push_notifications_enabled", defaultValue = "1") var pushNotificationsEnabled: Boolean,
  @ColumnInfo(name = "episodes_notifications_enabled", defaultValue = "1") var episodesNotificationsEnabled: Boolean,
  @ColumnInfo(name = "episodes_notifications_delay", defaultValue = "0") var episodesNotificationsDelay: Long,
  @ColumnInfo(name = "my_shows_recent_amount", defaultValue = "6") var myShowsRecentsAmount: Int,
  @ColumnInfo(name = "my_shows_running_sort_by", defaultValue = "NAME") var myShowsRunningSortBy: String,
  @ColumnInfo(name = "my_shows_incoming_sort_by", defaultValue = "NAME") var myShowsIncomingSortBy: String,
  @ColumnInfo(name = "my_shows_ended_sort_by", defaultValue = "NAME") var myShowsEndedSortBy: String,
  @ColumnInfo(name = "see_later_shows_sort_by", defaultValue = "NAME") var seeLaterShowsSortBy: String
)
