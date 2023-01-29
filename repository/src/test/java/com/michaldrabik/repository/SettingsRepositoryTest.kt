package com.michaldrabik.repository

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.SettingsDao
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.mappers.SettingsMapper
import com.michaldrabik.repository.settings.SettingsFiltersRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.settings.SettingsSortRepository
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.repository.settings.SettingsWidgetsRepository
import com.michaldrabik.ui_model.Settings
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest : BaseMockTest() {

  @MockK lateinit var settingsDao: SettingsDao
  @MockK lateinit var sharedPreferences: SharedPreferences
  @MockK lateinit var settingsSortRepository: SettingsSortRepository
  @MockK lateinit var settingsFilterRepository: SettingsFiltersRepository
  @MockK lateinit var settingsWidgetsRepository: SettingsWidgetsRepository
  @MockK lateinit var settingsViewModeRepository: SettingsViewModeRepository

  private lateinit var SUT: SettingsRepository

  @Before
  override fun setUp() {
    super.setUp()
    every { database.settings } returns settingsDao
    SUT = SettingsRepository(
      settingsSortRepository,
      settingsFilterRepository,
      settingsWidgetsRepository,
      settingsViewModeRepository,
      database,
      transactions,
      mappers,
      sharedPreferences
    )
  }

  @Test
  fun `Should be initialized if there are settings in database`() {
    runBlocking {
      coEvery { settingsDao.getCount() } returns 1
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
