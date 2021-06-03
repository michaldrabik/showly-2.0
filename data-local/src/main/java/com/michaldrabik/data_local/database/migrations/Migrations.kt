package com.michaldrabik.data_local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val DATABASE_VERSION = 28
const val DATABASE_NAME = "SHOWLY2_DB_2"

// TODO Split into separate files?
object Migrations {

  private val MIGRATION_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_shows INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE actors ADD COLUMN id_imdb TEXT")
    }
  }

  private val MIGRATION_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_sync_schedule TEXT NOT NULL DEFAULT 'OFF'")
    }
  }

  private val MIGRATION_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_running_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_incoming_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_ended_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_recent_is_enabled INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("CREATE INDEX index_episodes_id_show_trakt ON episodes(id_show_trakt)")
      database.execSQL("CREATE INDEX index_seasons_id_show_trakt ON seasons(id_show_trakt)")
    }
  }

  private val MIGRATION_7 = object : Migration(6, 7) {
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

  private val MIGRATION_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN watchlist_sort_by TEXT NOT NULL DEFAULT 'NAME'")
    }
  }

  private val MIGRATION_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_quick_remove_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val MIGRATION_10 = object : Migration(9, 10) {
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

  private val MIGRATION_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN archive_shows_include_statistics INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN special_seasons_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val MIGRATION_13 = object : Migration(12, 13) {
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

  private val MIGRATION_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE shows_images ADD COLUMN source TEXT NOT NULL DEFAULT 'tvdb'")
    }
  }

  private val MIGRATION_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
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
          "`id_movie_trakt` INTEGER PRIMARY KEY NOT NULL DEFAULT -1, " +
          "`synced_at` INTEGER NOT NULL DEFAULT 0)"
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_trakt_log` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`type` TEXT NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)"
      )
      database.execSQL("CREATE INDEX index_sync_trakt_log_id_trakt ON sync_trakt_log(id_trakt)")
      database.execSQL("CREATE INDEX index_sync_trakt_log_type ON sync_trakt_log(type)")
      database.execSQL("CREATE UNIQUE INDEX index_sync_trakt_log_id_trakt_type ON sync_trakt_log(id_trakt, type)")
    }
  }

  private val MIGRATION_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_collection_shows INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN show_collection_movies INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN widgets_show_label INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val MIGRATION_18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE actors ADD COLUMN id_tmdb_show INTEGER NOT NULL DEFAULT -1")
        execSQL("DELETE FROM actors")

        execSQL("ALTER TABLE shows_images ADD COLUMN id_tmdb INTEGER NOT NULL DEFAULT -1")
        execSQL("DELETE FROM shows_images WHERE source = 'tvdb' OR family = 'movie'")

        execSQL("CREATE INDEX index_shows_images_tmdb_id_type_family ON shows_images(id_tmdb, type, family)")
        execSQL("CREATE INDEX index_movies_images_tmdb_id_type ON movies_images(id_tmdb, type)")
      }
    }
  }

  private val MIGRATION_19 = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN my_movies_recent_is_enabled INTEGER NOT NULL DEFAULT 1")
      }
    }
  }

  private val MIGRATION_20 = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_images` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`family` TEXT NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`file_url` TEXT NOT NULL)"
        )
        execSQL("CREATE INDEX index_custom_images_trakt_id_family_type ON custom_images(id_trakt, family, type)")
      }
    }
  }

  private val MIGRATION_21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN quick_rate_enabled INTEGER NOT NULL DEFAULT 0")
      }
    }
  }

  private val MIGRATION_22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE shows ADD COLUMN created_at INTEGER NOT NULL DEFAULT -1")
        val cursor = database.query("SELECT id_trakt, updated_at FROM shows")
        while (cursor.moveToNext()) {
          val id = cursor.getLong(cursor.getColumnIndexOrThrow("id_trakt"))
          val updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
          execSQL("UPDATE shows SET created_at = $updatedAt WHERE id_trakt == $id")
        }
      }
    }
  }

  private val MIGRATION_23 = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_lists` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER, " +
            "`id_slug` TEXT NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`description` TEXT, " +
            "`privacy` TEXT NOT NULL, " +
            "`display_numbers` INTEGER NOT NULL, " +
            "`allow_comments` INTEGER NOT NULL, " +
            "`sort_by` TEXT NOT NULL, " +
            "`sort_how` TEXT NOT NULL, " +
            "`sort_by_local` TEXT NOT NULL, " +
            "`sort_how_local` TEXT NOT NULL, " +
            "`filter_type_local` TEXT NOT NULL, " +
            "`item_count` INTEGER NOT NULL, " +
            "`comment_count` INTEGER NOT NULL, " +
            "`likes` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL" +
            ")"
        )
        execSQL("CREATE UNIQUE INDEX index_custom_lists_id_trakt ON custom_lists(id_trakt)")
        execSQL("ALTER TABLE settings ADD COLUMN lists_sort_by TEXT NOT NULL DEFAULT 'DATE_UPDATED'")
      }
    }
  }

  private val MIGRATION_24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_list_item` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_list` INTEGER NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`rank` INTEGER NOT NULL, " +
            "`listed_at` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_list`) REFERENCES `custom_lists`(`id`) ON DELETE CASCADE" +
            ")"
        )
        execSQL("CREATE INDEX index_custom_list_item_id_list ON custom_list_item(id_list)")
        execSQL("CREATE INDEX index_custom_list_item_id_trakt_type ON custom_list_item(id_trakt, type)")
        execSQL("CREATE UNIQUE INDEX index_custom_list_item_id_list_id_trakt_type ON custom_list_item(id_list, id_trakt, type)")

        execSQL("ALTER TABLE trakt_sync_queue ADD COLUMN id_list INTEGER")
        execSQL("ALTER TABLE trakt_sync_queue ADD COLUMN operation TEXT NOT NULL DEFAULT ''")
      }
    }
  }

  private val MIGRATION_25 = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `news` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_news` TEXT NOT NULL, " +
            "`title` TEXT NOT NULL, " +
            "`url` TEXT NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`image` TEXT, " +
            "`score` INTEGER NOT NULL, " +
            "`dated_at` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL " +
            ")"
        )

        execSQL("ALTER TABLE user ADD COLUMN reddit_token TEXT NOT NULL DEFAULT ''")
        execSQL("ALTER TABLE user ADD COLUMN reddit_token_timestamp INTEGER NOT NULL DEFAULT 0")
      }
    }
  }

  private val MIGRATION_26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN progress_upcoming_enabled INTEGER NOT NULL DEFAULT 1")
      }
    }
  }

  private val MIGRATION_27 = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_ratings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`trakt` TEXT, " +
            "`imdb` TEXT, " +
            "`metascore` TEXT, " +
            "`rotten_tomatoes` TEXT, " +
            "`rotten_tomatoes_url` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
        )
        execSQL("CREATE UNIQUE INDEX index_movies_ratings_id_trakt ON movies_ratings(id_trakt)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `shows_ratings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`trakt` TEXT, " +
            "`imdb` TEXT, " +
            "`metascore` TEXT, " +
            "`rotten_tomatoes` TEXT, " +
            "`rotten_tomatoes_url` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)"
        )
        execSQL("CREATE UNIQUE INDEX index_shows_ratings_id_trakt ON shows_ratings(id_trakt)")
      }
    }
  }

  private val MIGRATION_28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_streamings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_tmdb` INTEGER NOT NULL, " +
            "`type` TEXT, " +
            "`provider_id` INTEGER, " +
            "`provider_name` TEXT, " +
            "`display_priority` INTEGER, " +
            "`logo_path` TEXT, " +
            "`link` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)"
        )
        execSQL("CREATE INDEX index_movies_streamings_id_trakt ON movies_streamings(id_trakt)")
        execSQL("CREATE INDEX index_movies_streamings_id_tmdb ON movies_streamings(id_tmdb)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `shows_streamings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_tmdb` INTEGER NOT NULL, " +
            "`type` TEXT, " +
            "`provider_id` INTEGER, " +
            "`provider_name` TEXT, " +
            "`display_priority` INTEGER, " +
            "`logo_path` TEXT, " +
            "`link` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)"
        )
        execSQL("CREATE INDEX index_shows_streamings_id_trakt ON shows_streamings(id_trakt)")
        execSQL("CREATE INDEX index_shows_streamings_id_tmdb ON shows_streamings(id_tmdb)")
      }
    }
  }

  val MIGRATIONS = listOf(
    MIGRATION_2,
    MIGRATION_3,
    MIGRATION_4,
    MIGRATION_5,
    MIGRATION_6,
    MIGRATION_7,
    MIGRATION_8,
    MIGRATION_9,
    MIGRATION_10,
    MIGRATION_11,
    MIGRATION_12,
    MIGRATION_13,
    MIGRATION_14,
    MIGRATION_15,
    MIGRATION_16,
    MIGRATION_17,
    MIGRATION_18,
    MIGRATION_19,
    MIGRATION_20,
    MIGRATION_21,
    MIGRATION_22,
    MIGRATION_23,
    MIGRATION_24,
    MIGRATION_25,
    MIGRATION_26,
    MIGRATION_27,
    MIGRATION_28
  )
}
