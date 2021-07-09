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
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_tmdb") var idTmdb: Long,
  @ColumnInfo(name = "type") var type: String?,
  @ColumnInfo(name = "provider_id") var providerId: Long?,
  @ColumnInfo(name = "provider_name") var providerName: String?,
  @ColumnInfo(name = "display_priority") var displayPriority: Long?,
  @ColumnInfo(name = "logo_path") var logoPath: String?,
  @ColumnInfo(name = "link") var link: String?,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime,
)
