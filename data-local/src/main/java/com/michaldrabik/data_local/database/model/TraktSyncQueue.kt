package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trakt_sync_queue")
data class TraktSyncQueue(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_list") var idList: Long?,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "operation") var operation: String,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
) {

  companion object {
    fun createEpisode(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, null, Type.EPISODE.slug, Operation.ADD.slug, createdAt, updatedAt)

    fun createShowWatchlist(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, null, Type.SHOW_WATCHLIST.slug, Operation.ADD.slug, createdAt, updatedAt)

    fun createMovie(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, null, Type.MOVIE.slug, Operation.ADD.slug, createdAt, updatedAt)

    fun createMovieWatchlist(
      idTrakt: Long,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, null, Type.MOVIE_WATCHLIST.slug, Operation.ADD.slug, createdAt, updatedAt)

    fun createListShow(
      idTrakt: Long,
      idList: Long,
      operation: Operation,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, idList, Type.LIST_ITEM_SHOW.slug, operation.slug, createdAt, updatedAt)

    fun createListMovie(
      idTrakt: Long,
      idList: Long,
      operation: Operation,
      createdAt: Long,
      updatedAt: Long
    ) = TraktSyncQueue(0, idTrakt, idList, Type.LIST_ITEM_MOVIE.slug, operation.slug, createdAt, updatedAt)
  }

  enum class Type(val slug: String) {
    EPISODE("episode"),
    SHOW_WATCHLIST("show_watchlist"),
    MOVIE("movie"),
    MOVIE_WATCHLIST("movie_watchlist"),
    LIST_ITEM_SHOW("list_item_show"),
    LIST_ITEM_MOVIE("list_item_movie"),
  }

  enum class Operation(val slug: String) {
    ADD("add"),
    REMOVE("remove")
  }
}
