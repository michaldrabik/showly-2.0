package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") var idTmdb: Long,
  @ColumnInfo(name = "id_imdb", defaultValue = "") var idImdb: String,
  @ColumnInfo(name = "id_slug", defaultValue = "") var idSlug: String,
  @ColumnInfo(name = "title", defaultValue = "") var title: String,
  @ColumnInfo(name = "year", defaultValue = "-1") var year: Int,
  @ColumnInfo(name = "overview", defaultValue = "") var overview: String,
  @ColumnInfo(name = "released", defaultValue = "") var released: String,
  @ColumnInfo(name = "runtime", defaultValue = "-1") var runtime: Int,
  @ColumnInfo(name = "country", defaultValue = "") var country: String,
  @ColumnInfo(name = "trailer", defaultValue = "") var trailer: String,
  @ColumnInfo(name = "language", defaultValue = "") var language: String,
  @ColumnInfo(name = "homepage", defaultValue = "") var homepage: String,
  @ColumnInfo(name = "status", defaultValue = "") var status: String,
  @ColumnInfo(name = "rating", defaultValue = "-1") var rating: Float,
  @ColumnInfo(name = "votes", defaultValue = "-1") var votes: Long,
  @ColumnInfo(name = "comment_count", defaultValue = "-1") var commentCount: Long,
  @ColumnInfo(name = "genres", defaultValue = "") var genres: String,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") var updatedAt: Long
)
