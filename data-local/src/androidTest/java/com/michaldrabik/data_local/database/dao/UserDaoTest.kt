@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndSaveData() {
    runBlocking {
      val testUser = User(1, "tvdbToken", 999, "test", "test", 999, "", "", 999)
      database.userDao().upsert(testUser)
      val result = database.userDao().get()
      assertThat(result).isEqualTo(testUser)
    }
  }

  @Test
  fun shouldUpdateRowIfAlreadyExists() {
    runBlocking {
      val testUser = User(1, "tvdbToken", 999, "test", "test", 999, "", "", 999)

      database.userDao().upsert(testUser)
      assertThat(database.userDao().get()).isEqualTo(testUser)

      val testUser2 = testUser.copy(tvdbToken = "otherToken", tvdbTokenTimestamp = 888)
      database.userDao().upsert(testUser2)
      assertThat(database.userDao().get()).isEqualTo(testUser2)
    }
  }
}
