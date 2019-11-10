@file:Suppress("DEPRECATION")

package com.michaldrabik.storage.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.storage.database.dao.helpers.TestData
import com.michaldrabik.storage.database.model.SeeLaterShow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeeLaterShowsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndStoreSingleEntity() {
    runBlocking {
      val show = TestData.createShow()
      val seeLaterShow = SeeLaterShow.fromTraktId(show.idTrakt, 999)

      database.showsDao().upsert(listOf(show))
      database.seeLaterShowsDao().insert(seeLaterShow)
      val result = database.seeLaterShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show))
    }
  }

  @Test
  fun shouldInsertAndStoreSingleEntityById() {
    runBlocking {
      val show = TestData.createShow()
      val show2 = TestData.createShow().copy(idTrakt = 2)
      val seeLaterShow = SeeLaterShow.fromTraktId(show.idTrakt, 999)
      val seeLaterShow2 = SeeLaterShow.fromTraktId(show2.idTrakt, 999)

      database.showsDao().upsert(listOf(show, show2))
      database.seeLaterShowsDao().insert(seeLaterShow)
      database.seeLaterShowsDao().insert(seeLaterShow2)

      val result = database.seeLaterShowsDao().getById(2)
      assertThat(result).isEqualTo(show2)
    }
  }

  @Test
  fun shouldDeleteSingleEntityById() {
    runBlocking {
      val show = TestData.createShow()
      val show2 = TestData.createShow().copy(idTrakt = 2)
      val seeLaterShow = SeeLaterShow.fromTraktId(show.idTrakt, 999)
      val seeLaterShow2 = SeeLaterShow.fromTraktId(show2.idTrakt, 999)

      database.showsDao().upsert(listOf(show, show2))
      database.seeLaterShowsDao().insert(seeLaterShow)
      database.seeLaterShowsDao().insert(seeLaterShow2)
      database.seeLaterShowsDao().deleteById(2)

      val result = database.seeLaterShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show))
    }
  }
}
