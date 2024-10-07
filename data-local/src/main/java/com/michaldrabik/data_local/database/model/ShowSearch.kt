package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class ShowSearch(
  @PrimaryKey @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "title") val title: String,
)
