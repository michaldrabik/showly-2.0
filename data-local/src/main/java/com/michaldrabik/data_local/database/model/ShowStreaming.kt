package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "shows_streamings",
  indices = [
    Index(value = ["id_trakt"]),
    Index(value = ["id_tmdb"]),
  ],
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt"),
      onDelete = ForeignKey.CASCADE
    )
  ]
)
@TypeConverters(DateConverter::class)
data class ShowStreaming(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_tmdb") val idTmdb: Long,
  @ColumnInfo(name = "type") val type: String?,
  @ColumnInfo(name = "provider_id") val providerId: Long?,
  @ColumnInfo(name = "provider_name") val providerName: String?,
  @ColumnInfo(name = "display_priority") val displayPriority: Long?,
  @ColumnInfo(name = "logo_path") val logoPath: String?,
  @ColumnInfo(name = "link") val link: String?,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
