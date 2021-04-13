@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndSaveData() {
    runBlocking {
      val settings = TestData.createSettings()

      database.settingsDao().upsert(settings)
      val result = database.settingsDao().getAll()
      assertThat(result).isEqualTo(settings)
    }
  }

  @Test
  fun shouldReturnNullIfNoEntity() {
    runBlocking {
      val settings = database.settingsDao().getAll()
      assertThat(settings).isNull()
    }
  }

  @Test
  fun shouldUpdateRowIfAlreadyExists() {
    runBlocking {
      val settings = TestData.createSettings()

      database.settingsDao().upsert(settings)
      assertThat(database.settingsDao().getAll()).isEqualTo(settings)

      val settings2 = settings.copy(myShowsEndedSortBy = "sort")
      database.settingsDao().upsert(settings2)
      assertThat(database.settingsDao().getAll()).isEqualTo(settings2)
    }
  }
}
