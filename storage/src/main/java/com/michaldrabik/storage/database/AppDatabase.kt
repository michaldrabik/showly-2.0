package com.michaldrabik.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michaldrabik.storage.database.dao.ShowsDao
import com.michaldrabik.storage.database.dao.TrendingShowsDao
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.TrendingShow

private const val DATABASE_VERSION = 1

@Database(entities = [Show::class, TrendingShow::class], version = DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {

  abstract fun showsDao(): ShowsDao

  abstract fun trendingShowsDao(): TrendingShowsDao
}