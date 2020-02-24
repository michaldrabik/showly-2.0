package com.michaldrabik.storage.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.DATABASE_NAME
import dagger.Module
import dagger.Provides
import timber.log.Timber

@Suppress("PrivatePropertyName")
@Module
class StorageModule(private val context: Context) {

  @Provides
  fun provideContext(): Context = context.applicationContext

  @Provides
  @StorageScope
  fun providesDatabase(context: Context): AppDatabase {
    Timber.d("Creating database...")
    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
      .addMigrations(MIGRATION_1_2)
      .build()
  }

  // TODO Extract migrations to SQL files if there are more.
  private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_shows INTEGER NOT NULL DEFAULT 1")
    }
  }
}
