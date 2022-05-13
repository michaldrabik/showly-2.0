@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.database.model.Show
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyShowsDaoTest : BaseDaoTest() {

  private val shows = mutableListOf<Show>()

  @Before
  fun setUp() = runBlocking {
    shows.add(TestData.createShow().copy(idTrakt = 1))
    shows.add(TestData.createShow().copy(idTrakt = 2))
    shows.add(TestData.createShow().copy(idTrakt = 3))

    database.showsDao().upsert(shows)
  }

  @Test
  fun shouldInsertAndStoreEntities() {
    runBlocking {
      val myShow = MyShow.fromTraktId(shows[0].idTrakt, 0, 0)

      database.myShowsDao().insert(listOf(myShow))

      val result = database.myShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(shows[0]))
    }
  }

  @Test
  fun shouldReturnIdsOnly() {
    runBlocking {
      val myShow1 = MyShow.fromTraktId(shows[0].idTrakt, 0, 0)
      val myShow2 = MyShow.fromTraktId(shows[1].idTrakt, 0, 0)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getAllTraktIds()
      assertThat(result).containsExactlyElementsIn(listOf(shows[0].idTrakt, shows[1].idTrakt))
    }
  }

  @Test
  fun shouldReturnMostRecentAddedShows() {
    runBlocking {
      val myShow1 = MyShow.fromTraktId(shows[0].idTrakt, 0, 0)
      val myShow2 = MyShow.fromTraktId(shows[1].idTrakt, 999, 999)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getAllRecent(10)
      assertThat(result[0]).isEqualTo(shows[1])
      assertThat(result[1]).isEqualTo(shows[0])
    }
  }

  @Test
  fun shouldReturnById() {
    runBlocking {
      val myShow1 = MyShow.fromTraktId(shows[0].idTrakt, 0, 0)
      val myShow2 = MyShow.fromTraktId(shows[1].idTrakt, 0, 0)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getById(shows[1].idTrakt)
      assertThat(result).isEqualTo(shows[1])
    }
  }

  @Test
  fun shouldDeleteByIdWithoutDeletingParent() {
    runBlocking {
      val myShow1 = MyShow.fromTraktId(shows[1].idTrakt, 0, 0)

      val showsSize = shows.size
      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().deleteById(shows[1].idTrakt)
      val result = database.myShowsDao().getById(shows[1].idTrakt)

      assertThat(result).isNull()
      assertThat(shows).hasSize(showsSize)
    }
  }
}
