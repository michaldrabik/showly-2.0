package com.michaldrabik.ui_trakt_sync

import com.michaldrabik.ui_model.TraktSyncSchedule
import java.time.format.DateTimeFormatter

data class TraktSyncUiState(
  val isProgress: Boolean = false,
  val progressStatus: String = "",
  val isAuthorized: Boolean = false,
  val traktSyncSchedule: TraktSyncSchedule = TraktSyncSchedule.OFF,
  val quickSyncEnabled: Boolean = false,
  val lastTraktSyncTimestamp: Long = 0L,
  val dateFormat: DateTimeFormatter? = null,
)
