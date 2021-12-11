package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "people_images",
  indices = [
    Index(value = ["id_tmdb"])
  ]
)
@TypeConverters(DateConverter::class)
data class PersonImage(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
  @ColumnInfo(name = "id_tmdb") var idTmdb: Long,
  @ColumnInfo(name = "file_path") var filePath: String,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime
)
