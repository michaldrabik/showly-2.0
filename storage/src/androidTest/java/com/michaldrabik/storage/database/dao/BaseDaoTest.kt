@file:Suppress("DEPRECATION")

package com.michaldrabik.storage.database.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.michaldrabik.storage.database.AppDatabase
import org.junit.After
import org.junit.Before

abstract class BaseDaoTest {

  protected lateinit var database: AppDatabase

  @Before
  fun initDb() {
    database = Room.inMemoryDatabaseBuilder(
      InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
      AppDatabase::class.java
    ).build()
  }

  @After
  fun closeDb() = database.close()
}
