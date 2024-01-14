@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeasonsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndStoreSingleEntity() {
    runBlocking {
      val season = TestData.createSeason()

      database.seasonsDao().upsert(listOf(season))
      val result = database.seasonsDao().getAllByShowId(1)
      assertThat(result).containsExactlyElementsIn(listOf(season))
    }
  }

  @Test
  fun shouldInsertAndStoreMultipleEntities() {
    runBlocking {
      val season1 = TestData.createSeason().copy(idTrakt = 1)
      val season2 = TestData.createSeason().copy(idTrakt = 2)

      database.seasonsDao().upsert(listOf(season1, season2))
      val result = database.seasonsDao().getAllByShowId(1)
      assertThat(result).containsExactlyElementsIn(listOf(season1, season2))
    }
  }

  @Test
  fun shouldReturnEntityById() {
    runBlocking {
      val season1 = TestData.createSeason().copy(idTrakt = 1)
      val season2 = TestData.createSeason().copy(idTrakt = 2)

      database.seasonsDao().upsert(listOf(season1, season2))
      val result = database.seasonsDao().getById(2)
      assertThat(result).isEqualTo(season2)
    }
  }

  @Test
  fun shouldReturnEntitiesByIds() {
    runBlocking {
      val season1 = TestData.createSeason().copy(idTrakt = 1)
      val season2 = TestData.createSeason().copy(idTrakt = 2, idShowTrakt = 2)
      val season3 = TestData.createSeason().copy(idTrakt = 3)

      database.seasonsDao().upsert(listOf(season1, season2, season3))
      val result = database.seasonsDao().getAllByShowId(1)
      assertThat(result).containsExactlyElementsIn(listOf(season1, season3))
    }
  }

  @Test
  fun shouldOnlyReturnWatchedSeasons() {
    runBlocking {
      val season1 = TestData.createSeason().copy(idTrakt = 1)
      val season2 = TestData.createSeason().copy(idTrakt = 2)
      val season3 = TestData.createSeason().copy(idTrakt = 3, isWatched = true)

      database.seasonsDao().upsert(listOf(season1, season2, season3))
      val result = database.seasonsDao().getAllWatchedIdsForShows(listOf(1))
      assertThat(result).hasSize(1)
      assertThat(result[0]).isEqualTo(3)
    }
  }

  @Test
  fun shouldReturnNullIfNoEntity() {
    runBlocking {
      val season1 = TestData.createSeason()

      database.seasonsDao().upsert(listOf(season1))
      val result = database.seasonsDao().getById(2)
      assertThat(result).isNull()
    }
  }
}
