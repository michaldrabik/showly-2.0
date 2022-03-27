package com.michaldrabik.repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.ArchiveShowsDao
import com.michaldrabik.data_local.database.dao.MyShowsDao
import com.michaldrabik.data_local.database.dao.WatchlistShowsDao
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.shows.WatchlistShowsRepository
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

class WatchlistShowsRepositoryTest : BaseMockTest() {

  @MockK lateinit var seeLaterShowsDao: WatchlistShowsDao
  @MockK lateinit var myShowsDao: MyShowsDao
  @MockK lateinit var archivedShowsDao: ArchiveShowsDao

  @RelaxedMockK lateinit var showDb: ShowDb

  private lateinit var SUT: WatchlistShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = WatchlistShowsRepository(database, transactions, mappers)

    coEvery { database.watchlistShows } returns seeLaterShowsDao
    coEvery { database.myShows } returns myShowsDao
    coEvery { database.archiveShows } returns archivedShowsDao
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(seeLaterShowsDao)
  }

  @Test
  fun `Should load and map all SeeLater shows`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAll() } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      SUT.loadAll()

      coVerify(exactly = 1) { seeLaterShowsDao.getAll() }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map single SeeLater show by Trakt ID`() {
    runBlocking {
      val show = Show.EMPTY.copy(title = "Test")

      coEvery { seeLaterShowsDao.getById(any()) } returns showDb
      coEvery { mappers.show.fromDatabase(any()) } returns show

      val testShow = SUT.load(IdTrakt(1L))

      assertThat(testShow?.title).isEqualTo(show.title)
      coVerify(exactly = 1) { seeLaterShowsDao.getById(any()) }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should insert show into database using Trakt ID`() {
    runBlocking {
      coJustRun { myShowsDao.deleteById(any()) }
      coJustRun { archivedShowsDao.deleteById(any()) }

      val slot = slot<WatchlistShow>()
      coJustRun { seeLaterShowsDao.insert(capture(slot)) }

      SUT.insert(IdTrakt(1L))

      assertThat(slot.captured.id).isEqualTo(0)
      assertThat(slot.captured.idTrakt).isEqualTo(1)

      coVerify(exactly = 1) { seeLaterShowsDao.insert(any()) }
    }
  }

  @Test
  fun `Should delete show from archived and my shows when inserting into see later`() {
    runBlocking {
      coJustRun { myShowsDao.deleteById(any()) }
      coJustRun { archivedShowsDao.deleteById(any()) }

      val slot = slot<WatchlistShow>()
      coJustRun { seeLaterShowsDao.insert(capture(slot)) }

      SUT.insert(IdTrakt(1L))

      assertThat(slot.captured.id).isEqualTo(0)
      assertThat(slot.captured.idTrakt).isEqualTo(1)

      coVerify(exactly = 1) { seeLaterShowsDao.insert(any()) }
      coVerify(exactly = 1) { myShowsDao.deleteById(1L) }
      coVerify(exactly = 1) { archivedShowsDao.deleteById(1L) }
    }
  }

  @Test
  fun `Should delete show from database using Trakt ID`() {
    runBlocking {
      val slot = slot<Long>()
      coJustRun { seeLaterShowsDao.deleteById(capture(slot)) }

      SUT.delete(IdTrakt(10L))

      assertThat(slot.captured).isEqualTo(10L)
      coVerify(exactly = 1) { seeLaterShowsDao.deleteById(10L) }
    }
  }

  @Test
  fun `Should load all SeeLater shows ids`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAllTraktIds() } returns listOf(1L, 2L)

      val ids = SUT.loadAllIds()

      assertThat(ids).containsExactly(1L, 2L)
      coVerify(exactly = 1) { seeLaterShowsDao.getAllTraktIds() }
    }
  }
}
