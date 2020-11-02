package com.michaldrabik.storage.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val DATABASE_VERSION = 13
const val DATABASE_NAME = "SHOWLY2_DB_2"

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

  private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_running_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_incoming_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_ended_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_recent_is_enabled INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("CREATE INDEX index_episodes_id_show_trakt ON episodes(id_show_trakt)")
      database.execSQL("CREATE INDEX index_seasons_id_show_trakt ON seasons(id_show_trakt)")
    }
  }

  private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_filter_genres TEXT NOT NULL DEFAULT ''")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_filter_feed TEXT NOT NULL DEFAULT 'HOT'")
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_quick_sync_enabled INTEGER NOT NULL DEFAULT 0")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `trakt_sync_queue` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `type` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)"
      )
    }
  }

  private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN watchlist_sort_by TEXT NOT NULL DEFAULT 'NAME'")
    }
  }

  private val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_quick_remove_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `shows_archive` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE UNIQUE INDEX index_shows_archive_id_trakt ON shows_archive(id_trakt)")
      database.execSQL("ALTER TABLE settings ADD COLUMN archive_shows_sort_by TEXT NOT NULL DEFAULT 'NAME'")
    }
  }

  private val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN archive_shows_include_statistics INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN special_seasons_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `shows_translations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `title` TEXT NOT NULL, `language` TEXT NOT NULL, `overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE UNIQUE INDEX index_shows_translations_id_trakt ON shows_translations(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `episodes_translations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `id_trakt_show` INTEGER NOT NULL, " +
          "`title` TEXT NOT NULL, `language` TEXT NOT NULL, `overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt_show`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE UNIQUE INDEX index_episodes_translations_id_trakt ON episodes_translations(id_trakt)")
      database.execSQL("CREATE INDEX index_episodes_translations_id_trakt_show ON episodes_translations(id_trakt_show)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_translations_log` (`id_show_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)"
      )
    }
  }

  val MIGRATIONS = listOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13
  )
}
