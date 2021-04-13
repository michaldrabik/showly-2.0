@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActorsDaoTest : BaseDaoTest() {

  private val actor by lazy { TestData.createActor() }

  @Test
  fun shouldInsertSingleEntity() {
    runBlocking {
      val actor = actor.copy(id = 99, idShowTmdb = 22)

      database.actorsDao().upsert(listOf(actor))
      val result = database.actorsDao().getAllByShow(22)
      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(actor)
    }
  }

  @Test
  fun shouldInsertMultipleEntities() {
    runBlocking {
      val actor1 = actor.copy(id = 99, idShowTmdb = 22)
      val actor2 = actor.copy(id = 23, idShowTmdb = 22)

      database.actorsDao().upsert(listOf(actor1, actor2))
      val result = database.actorsDao().getAllByShow(22)
      assertThat(result).hasSize(2)
      assertThat(result).containsExactlyElementsIn(listOf(actor1, actor2))
    }
  }

  @Test
  fun shouldUpdateEntitiesIfExist() {
    runBlocking {
      val actor1 = actor.copy(id = 99, idShowTmdb = 22)
      val actor2 = actor.copy(id = 23, idShowTmdb = 22)

      val actor1Updated = actor1.copy(id = 99, idShowTmdb = 99)
      val actor2Updated = actor2.copy(id = 23, idShowTmdb = 99)

      database.actorsDao().upsert(listOf(actor1, actor2))
      database.actorsDao().upsert(listOf(actor1Updated, actor2Updated))
      val result = database.actorsDao().getAllByShow(99)
      assertThat(result).hasSize(2)
      assertThat(result).containsExactlyElementsIn(listOf(actor1Updated, actor2Updated))
    }
  }

  @Test
  fun shouldDeleteEntitiesByShowId() {
    runBlocking {
      val actor1 = actor.copy(id = 99, idShowTmdb = 11)
      val actor2 = actor.copy(id = 23, idShowTmdb = 22)

      database.actorsDao().upsert(listOf(actor1, actor2))
      assertThat(database.actorsDao().getAllByShow(11)).containsExactly(actor1)
      assertThat(database.actorsDao().getAllByShow(22)).containsExactly(actor2)

      database.actorsDao().deleteAllByShow(11)
      assertThat(database.actorsDao().getAllByShow(11)).isEmpty()
      assertThat(database.actorsDao().getAllByShow(22)).containsExactly(actor2)
    }
  }

  @Test
  fun shouldReplaceEntitiesByShowId() {
    runBlocking {
      val actor1 = actor.copy(id = 11, idShowTmdb = 111)
      val actor2 = actor.copy(id = 22, idShowTmdb = 111)
      val actor3 = actor.copy(id = 33, idShowTmdb = 333)

      database.actorsDao().upsert(listOf(actor1, actor2, actor3))
      assertThat(database.actorsDao().getAllByShow(111)).containsExactly(actor1, actor2)
      assertThat(database.actorsDao().getAllByShow(333)).containsExactly(actor3)

      val actor4 = actor.copy(id = 44, idShowTmdb = 111)
      val actor5 = actor.copy(id = 55, idShowTmdb = 111)

      database.actorsDao().replaceForShow(listOf(actor4, actor5), 111)
      assertThat(database.actorsDao().getAllByShow(111)).containsExactly(actor4, actor5)
      assertThat(database.actorsDao().getAllByShow(333)).containsExactly(actor3)
    }
  }
}
