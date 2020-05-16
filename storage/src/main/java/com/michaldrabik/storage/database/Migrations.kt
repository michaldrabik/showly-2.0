package com.michaldrabik.storage.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

  private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_shows INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE actors ADD COLUMN id_imdb TEXT")
    }
  }

  private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_sync_schedule TEXT NOT NULL DEFAULT 'OFF'")
    }
  }

  val MIGRATIONS = listOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4
  )
}
