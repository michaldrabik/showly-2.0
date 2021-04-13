package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows")
data class Show(
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_tvdb", defaultValue = "-1") var idTvdb: Long,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") var idTmdb: Long,
  @ColumnInfo(name = "id_imdb", defaultValue = "") var idImdb: String,
  @ColumnInfo(name = "id_slug", defaultValue = "") var idSlug: String,
  @ColumnInfo(name = "id_tvrage", defaultValue = "-1") var idTvrage: Long,
  @ColumnInfo(name = "title", defaultValue = "") var title: String,
  @ColumnInfo(name = "year", defaultValue = "-1") var year: Int,
  @ColumnInfo(name = "overview", defaultValue = "") var overview: String,
  @ColumnInfo(name = "first_aired", defaultValue = "") var firstAired: String,
  @ColumnInfo(name = "runtime", defaultValue = "-1") var runtime: Int,
  @ColumnInfo(name = "airtime_day", defaultValue = "") var airtimeDay: String,
  @ColumnInfo(name = "airtime_time", defaultValue = "") var airtimeTime: String,
  @ColumnInfo(name = "airtime_timezone", defaultValue = "") var airtimeTimezone: String,
  @ColumnInfo(name = "certification", defaultValue = "") var certification: String,
  @ColumnInfo(name = "network", defaultValue = "") var network: String,
  @ColumnInfo(name = "country", defaultValue = "") var country: String,
  @ColumnInfo(name = "trailer", defaultValue = "") var trailer: String,
  @ColumnInfo(name = "homepage", defaultValue = "") var homepage: String,
  @ColumnInfo(name = "status", defaultValue = "") var status: String,
  @ColumnInfo(name = "rating", defaultValue = "-1") var rating: Float,
  @ColumnInfo(name = "votes", defaultValue = "-1") var votes: Long,
  @ColumnInfo(name = "comment_count", defaultValue = "-1") var commentCount: Long,
  @ColumnInfo(name = "genres", defaultValue = "") var genres: String,
  @ColumnInfo(name = "aired_episodes", defaultValue = "-1") var airedEpisodes: Int,
  @ColumnInfo(name = "created_at", defaultValue = "-1") var createdAt: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") var updatedAt: Long
)
