package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows")
data class Show(
  @PrimaryKey @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_tvdb", defaultValue = "-1") val idTvdb: Long,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") val idTmdb: Long,
  @ColumnInfo(name = "id_imdb", defaultValue = "") val idImdb: String,
  @ColumnInfo(name = "id_slug", defaultValue = "") val idSlug: String,
  @ColumnInfo(name = "id_tvrage", defaultValue = "-1") val idTvrage: Long,
  @ColumnInfo(name = "title", defaultValue = "") val title: String,
  @ColumnInfo(name = "year", defaultValue = "-1") val year: Int,
  @ColumnInfo(name = "overview", defaultValue = "") val overview: String,
  @ColumnInfo(name = "first_aired", defaultValue = "") val firstAired: String,
  @ColumnInfo(name = "runtime", defaultValue = "-1") val runtime: Int,
  @ColumnInfo(name = "airtime_day", defaultValue = "") val airtimeDay: String,
  @ColumnInfo(name = "airtime_time", defaultValue = "") val airtimeTime: String,
  @ColumnInfo(name = "airtime_timezone", defaultValue = "") val airtimeTimezone: String,
  @ColumnInfo(name = "certification", defaultValue = "") val certification: String,
  @ColumnInfo(name = "network", defaultValue = "") val network: String,
  @ColumnInfo(name = "country", defaultValue = "") val country: String,
  @ColumnInfo(name = "trailer", defaultValue = "") val trailer: String,
  @ColumnInfo(name = "homepage", defaultValue = "") val homepage: String,
  @ColumnInfo(name = "status", defaultValue = "") val status: String,
  @ColumnInfo(name = "rating", defaultValue = "-1") val rating: Float,
  @ColumnInfo(name = "votes", defaultValue = "-1") val votes: Long,
  @ColumnInfo(name = "comment_count", defaultValue = "-1") val commentCount: Long,
  @ColumnInfo(name = "genres", defaultValue = "") val genres: String,
  @ColumnInfo(name = "aired_episodes", defaultValue = "-1") val airedEpisodes: Int,
  @ColumnInfo(name = "created_at", defaultValue = "-1") val createdAt: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long
)
