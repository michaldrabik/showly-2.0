package com.michaldrabik.storage.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val DATABASE_VERSION = 15
const val DATABASE_NAME = "SHOWLY2_DB_2"

//TODO Split into separate files
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
        "CREATE TABLE IF NOT EXISTS `trakt_sync_queue` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`type` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL)"
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
        "CREATE TABLE IF NOT EXISTS `shows_archive` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL, " +
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
        "CREATE TABLE IF NOT EXISTS `sync_translations_log` (" +
          "`id_show_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)"
      )
    }
  }

  private val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE shows_images ADD COLUMN source TEXT NOT NULL DEFAULT 'tvdb'")
    }
  }

  private val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN movies_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN movies_active INTEGER NOT NULL DEFAULT 0")
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_movies INTEGER NOT NULL DEFAULT 0")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_movies_filter_genres TEXT NOT NULL DEFAULT ''")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_movies_filter_feed TEXT NOT NULL DEFAULT 'HOT'")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_movies_all_sort_by TEXT NOT NULL DEFAULT 'NAME'")
      database.execSQL("ALTER TABLE settings ADD COLUMN see_later_movies_sort_by TEXT NOT NULL DEFAULT 'NAME'")
      database.execSQL("ALTER TABLE settings ADD COLUMN progress_movies_sort_by TEXT NOT NULL DEFAULT 'NAME'")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies` (" +
          "`id_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`id_tmdb` INTEGER NOT NULL DEFAULT -1, " +
          "`id_imdb` TEXT NOT NULL DEFAULT '', " +
          "`id_slug` TEXT NOT NULL DEFAULT '', " +
          "`title` TEXT NOT NULL DEFAULT '', " +
          "`year` INTEGER NOT NULL DEFAULT -1, " +
          "`overview` TEXT NOT NULL DEFAULT '', " +
          "`released` TEXT NOT NULL DEFAULT '', " +
          "`runtime` INTEGER NOT NULL DEFAULT -1, " +
          "`country` TEXT NOT NULL DEFAULT '', " +
          "`trailer` TEXT NOT NULL DEFAULT '', " +
          "`language` TEXT NOT NULL DEFAULT '', " +
          "`homepage` TEXT NOT NULL DEFAULT '', " +
          "`status` TEXT NOT NULL DEFAULT '', " +
          "`rating` REAL NOT NULL DEFAULT -1, " +
          "`votes` INTEGER NOT NULL DEFAULT -1, " +
          "`comment_count` INTEGER NOT NULL DEFAULT -1, " +
          "`genres` TEXT NOT NULL DEFAULT '', " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1)"
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_discover` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE INDEX index_discover_movies_id_trakt ON movies_discover(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_images` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_tmdb` INTEGER NOT NULL DEFAULT -1, " +
          "`type` TEXT NOT NULL DEFAULT '', " +
          "`file_url` TEXT NOT NULL DEFAULT '', " +
          "`source` TEXT NOT NULL DEFAULT 'tmdb')"
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_translations` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
          "`language` TEXT NOT NULL, " +
          "`overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE UNIQUE INDEX index_movies_translations_id_trakt ON movies_translations(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_movies_translations_log` (" +
          "`id_movie_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)"
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_related` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL  DEFAULT -1, " +
          "`id_trakt_related_movie` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt_related_movie`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE INDEX index_movies_related_id_trakt ON movies_related(id_trakt_related_movie)")

      database.execSQL("ALTER TABLE actors ADD COLUMN id_tmdb_movie INTEGER NOT NULL DEFAULT -1")
      database.execSQL("ALTER TABLE actors ADD COLUMN id_tmdb INTEGER NOT NULL DEFAULT -1")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_my_movies` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE INDEX index_movies_my_movies_id_trakt ON movies_my_movies(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_see_later` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
      )
      database.execSQL("CREATE INDEX index_movies_see_later_id_trakt ON movies_see_later(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_movies_log` (" +
          "`id_movie_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL DEFAULT 0)"
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
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15
  )
}
