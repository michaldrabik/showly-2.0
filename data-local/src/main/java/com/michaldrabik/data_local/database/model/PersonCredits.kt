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
  tableName = "people_credits",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_show"),
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_trakt_movie"),
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["id_trakt_person"])
  ]
)
@TypeConverters(DateConverter::class)
data class PersonCredits(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
  @ColumnInfo(name = "id_trakt_person") var idTraktPerson: Long,
  @ColumnInfo(name = "id_trakt_show") var idTraktShow: Long?,
  @ColumnInfo(name = "id_trakt_movie") var idTraktMovie: Long?,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime
)
