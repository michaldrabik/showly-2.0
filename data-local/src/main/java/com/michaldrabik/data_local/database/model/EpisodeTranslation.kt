package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "episodes_translations",
  indices = [
    Index(value = ["id_trakt"], unique = true),
    Index(value = ["id_trakt_show"])
  ],
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_show"),
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class EpisodeTranslation(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_trakt_show") var idTraktShow: Long,
  @ColumnInfo(name = "title") var title: String,
  @ColumnInfo(name = "language") var language: String,
  @ColumnInfo(name = "overview") var overview: String,
  @ColumnInfo(name = "created_at") var createdAt: Long,
  @ColumnInfo(name = "updated_at") var updatedAt: Long
) {

  companion object {
    fun fromTraktId(
      traktEpisodeId: Long,
      traktShowId: Long,
      title: String,
      language: String,
      overview: String,
      createdAt: Long
    ) =
      EpisodeTranslation(
        idTrakt = traktEpisodeId,
        idTraktShow = traktShowId,
        title = title,
        language = language,
        overview = overview,
        createdAt = createdAt,
        updatedAt = createdAt
      )
  }
}
