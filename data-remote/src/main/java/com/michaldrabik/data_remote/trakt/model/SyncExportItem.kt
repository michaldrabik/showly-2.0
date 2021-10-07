package com.michaldrabik.data_remote.trakt.model

data class SyncExportItem(
  val ids: Ids,
  val watched_at: String?,
  val hidden_at: String?,
) {

  companion object {
    fun create(
      traktId: Long,
      watchedAt: String? = "released",
      hiddenAt: String? = null,
    ) = SyncExportItem(Ids(traktId), watchedAt, hiddenAt)
  }

  data class Ids(
    val trakt: Long
  )
}
