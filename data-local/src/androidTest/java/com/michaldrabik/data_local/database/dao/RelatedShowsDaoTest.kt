@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import com.michaldrabik.data_local.database.model.RelatedShow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RelatedShowsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndDeleteSingleEntity() {
    runBlocking {
      val show = TestData.createShow().copy(idTrakt = 99)
      val relatedShow = RelatedShow(1, 1, 99, 999)

      database.showsDao().upsert(listOf(show))
      database.relatedShowsDao().insert(listOf(relatedShow))
      val result = database.relatedShowsDao().getAll()
      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(relatedShow)

      database.relatedShowsDao().deleteById(99)
      val result2 = database.relatedShowsDao().getAll()
      assertThat(result2).isEmpty()
      assertThat(database.showsDao().getById(99)).isEqualTo(show)
    }
  }

  @Test
  fun shouldReturnRelatedShowsForId() {
    runBlocking {
      val show1 = TestData.createShow().copy(idTrakt = 1, updatedAt = 100)
      val show2 = TestData.createShow().copy(idTrakt = 2, updatedAt = 100)
      val show3 = TestData.createShow().copy(idTrakt = 3, updatedAt = 100)

      val relatedShow1 = RelatedShow(1, 2, 1, 200)
      val relatedShow2 = RelatedShow(2, 3, 1, 200)
      val relatedShow3 = RelatedShow(3, 1, 2, 200)

      database.showsDao().upsert(listOf(show1, show2, show3))
      database.relatedShowsDao().insert(listOf(relatedShow1, relatedShow2, relatedShow3))
      val result = database.relatedShowsDao().getAllById(1)
      assertThat(result).containsExactlyElementsIn(listOf(relatedShow1, relatedShow2))
    }
  }
}
