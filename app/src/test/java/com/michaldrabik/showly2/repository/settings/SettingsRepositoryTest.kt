package com.michaldrabik.showly2.repository.settings

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.model.mappers.SettingsMapper
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.dao.SettingsDao
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

  @MockK lateinit var database: AppDatabase
  @MockK lateinit var settingsDao: SettingsDao
  @MockK lateinit var mappers: Mappers

  private lateinit var SUT: SettingsRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { database.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }

    every { database.settingsDao() } returns settingsDao

    SUT = SettingsRepository(database, mappers)
  }

  @Test
  fun `Should be initialized if there are settings in database`() {
    runBlocking {
      coEvery { settingsDao.getCount() } returns 0
      assertThat(SUT.isInitialized()).isTrue()
    }
  }

  @Test
  fun `Should not be initialized if there are no settings in database`() {
    runBlocking {
      coEvery { settingsDao.getCount() } returns 0
      assertThat(SUT.isInitialized()).isFalse()
    }
  }

  @Test
  fun `Should load settings properly`() {
    runBlocking {
      val mapper = SettingsMapper()
      val settings = Settings.createInitial()
      val settingsDb = mapper.toDatabase(settings)

      coEvery { mappers.settings } returns mapper
      coEvery { settingsDao.getAll() } returns settingsDb

      val loaded = SUT.load()

      assertThat(loaded).isEqualTo(settings)
      coVerify { settingsDao.getAll() }
      confirmVerified(settingsDao)
    }
  }

  @Test
  fun `Should update settings properly`() {
    runBlocking {
      val mapper = SettingsMapper()
      val settings = Settings.createInitial()
      val settingsDb = mapper.toDatabase(settings)

      coEvery { mappers.settings } returns mapper
      coEvery { settingsDao.upsert(settingsDb) } just Runs

      SUT.update(settings)

      coVerify { settingsDao.upsert(settingsDb) }
      confirmVerified(settingsDao)
    }
  }
}
