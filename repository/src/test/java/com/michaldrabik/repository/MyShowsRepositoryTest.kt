package com.michaldrabik.repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.MyShowsDao
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.shows.MyShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.michaldrabik.data_local.database.model.Show as ShowDb

class MyShowsRepositoryTest : BaseMockTest() {

  @MockK lateinit var myShowsDao: MyShowsDao
  @RelaxedMockK lateinit var showDb: ShowDb

  private lateinit var SUT: MyShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = MyShowsRepository(database, mappers)
    coEvery { database.myShowsDao() } returns myShowsDao
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(myShowsDao)
  }

  @Test
  fun `Should load and map single show by Trakt ID`() {
    runBlocking {
      val show = Show.EMPTY.copy(title = "Test")

      coEvery { myShowsDao.getById(any()) } returns showDb
      coEvery { mappers.show.fromDatabase(any()) } returns show

      val testShow = SUT.load(IdTrakt(1L))

      assertThat(testShow?.title).isEqualTo(show.title)
      coVerify(exactly = 1) { myShowsDao.getById(any()) }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map all shows`() {
    runBlocking {
      coEvery { myShowsDao.getAll() } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      SUT.loadAll()

      coVerify(exactly = 1) { myShowsDao.getAll() }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load all shows ids`() {
    runBlocking {
      coEvery { myShowsDao.getAllTraktIds() } returns listOf(1L, 2L)

      val ids = SUT.loadAllIds()

      assertThat(ids).containsExactly(1L, 2L)
      coVerify(exactly = 1) { myShowsDao.getAllTraktIds() }
    }
  }

  @Test
  fun `Should load and map all shows by Trakt Ids`() {
    runBlocking {
      coEvery { myShowsDao.getAll(any()) } returns listOf(showDb, showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      val shows = SUT.loadAll(listOf(IdTrakt(1), IdTrakt(2)))

      assertThat(shows).hasSize(2)
      coVerify(exactly = 1) { myShowsDao.getAll(listOf(1, 2)) }
      coVerify(exactly = 2) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map all recents shows using amount`() {
    runBlocking {
      coEvery { myShowsDao.getAllRecent(any()) } returns listOf(showDb, showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      val shows = SUT.loadAllRecent(2)

      assertThat(shows).hasSize(2)
      coVerify(exactly = 1) { myShowsDao.getAllRecent(2) }
      coVerify(exactly = 2) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should insert show into database using Trakt ID`() {
    runBlocking {
      val slot = slot<List<MyShow>>()
      coJustRun { myShowsDao.insert(capture(slot)) }

      SUT.insert(IdTrakt(10L))

      slot.captured[0].run {
        assertThat(id).isEqualTo(0)
        assertThat(idTrakt).isEqualTo(10)
        assertThat(createdAt).isGreaterThan(0)
        assertThat(updatedAt).isEqualTo(0)
      }
      coVerify(exactly = 1) { myShowsDao.insert(any()) }
    }
  }

  @Test
  fun `Should delete show from database using Trakt ID`() {
    runBlocking {
      val slot = slot<Long>()
      coJustRun { myShowsDao.deleteById(capture(slot)) }

      SUT.delete(IdTrakt(10L))

      assertThat(slot.captured).isEqualTo(10L)
      coVerify(exactly = 1) { myShowsDao.deleteById(10L) }
    }
  }
}
