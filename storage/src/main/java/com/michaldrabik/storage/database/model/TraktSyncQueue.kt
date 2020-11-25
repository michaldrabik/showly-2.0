package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trakt_sync_queue")
data class TraktSyncQueue(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
) {

  companion object {
    fun createEpisode(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, Type.EPISODE.slug, createdAt, updatedAt)

    fun createShowWatchlist(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, Type.SHOW_WATCHLIST.slug, createdAt, updatedAt)
  }

  enum class Type(val slug: String) {
    EPISODE("episode"),
    SHOW_WATCHLIST("show_watchlist")
  }
}
