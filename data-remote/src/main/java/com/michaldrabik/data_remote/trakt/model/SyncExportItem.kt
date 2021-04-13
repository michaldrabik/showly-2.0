package com.michaldrabik.data_remote.trakt.model

data class SyncExportItem(
  val ids: Ids,
  val watched_at: String?
) {

  companion object {
    fun create(
      traktId: Long,
      watchedAt: String? = "released"
    ) = SyncExportItem(Ids(traktId), watchedAt)
  }

  data class Ids(
    val trakt: Long
  )
}
