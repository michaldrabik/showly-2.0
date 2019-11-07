@file:Suppress("DEPRECATION")

package com.michaldrabik.storage.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.storage.database.model.DiscoverShow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscoverShowsDaoTest : BaseDaoTest() {

  private val show by lazy { DiscoverShow(1, 11, 999, 999) }

  @Test
  fun shouldInsertSingleEntity() {
    runBlocking {
      database.discoverShowsDao().upsert(listOf(show))
      val result = database.discoverShowsDao().getAll()
      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(show)
    }
  }

  @Test
  fun shouldInsertMultipleEntities() {
    runBlocking {
      val show1 = show.copy(id = 11)
      val show2 = show.copy(id = 12)

      database.discoverShowsDao().upsert(listOf(show1, show2))
      val result = database.discoverShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show1, show2))
    }
  }

  @Test
  fun shouldUpdateEntitiesIfExist() {
    runBlocking {
      val show1 = show.copy(id = 99)
      val show2 = show.copy(id = 23)

      val show1Updated = show1.copy(updatedAt = 1000)
      val show2Updated = show2.copy(updatedAt = 1000)

      database.discoverShowsDao().upsert(listOf(show1, show2))
      database.discoverShowsDao().upsert(listOf(show1Updated, show2Updated))
      val result = database.discoverShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show1Updated, show2Updated))
    }
  }

  @Test
  fun shouldReplaceEntities() {
    runBlocking {
      val show1 = show.copy(id = 11)
      val show2 = show.copy(id = 22)
      val show3 = show.copy(id = 33)

      database.discoverShowsDao().upsert(listOf(show1, show2, show3))
      assertThat(database.discoverShowsDao().getAll()).containsExactly(show1, show2, show3)

      val show4 = show.copy(id = 44)

      database.discoverShowsDao().replace(listOf(show4))
      assertThat(database.discoverShowsDao().getAll()).containsExactly(show4)
    }
  }

  @Test
  fun shouldReturnMostRecentEntity() {
    runBlocking {
      val show1 = show.copy(id = 11, createdAt = 2)
      val show2 = show.copy(id = 22, createdAt = 3)
      val show3 = show.copy(id = 33, createdAt = 1)

      database.discoverShowsDao().upsert(listOf(show1, show2, show3))
      val mostRecent = database.discoverShowsDao().getMostRecent()

      assertThat(mostRecent).isEqualTo(show2)
    }
  }
}
