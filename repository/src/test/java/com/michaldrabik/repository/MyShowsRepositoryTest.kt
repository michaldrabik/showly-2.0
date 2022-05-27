package com.michaldrabik.repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.sources.ArchiveShowsLocalDataSource
import com.michaldrabik.data_local.sources.MyShowsLocalDataSource
import com.michaldrabik.data_local.sources.WatchlistShowsLocalDataSource
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.shows.MyShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.michaldrabik.data_local.database.model.Show as ShowDb

class MyShowsRepositoryTest : BaseMockTest() {

  @RelaxedMockK lateinit var myShowsLocalSource: MyShowsLocalDataSource
  @RelaxedMockK lateinit var watchlistShowsLocalSource: WatchlistShowsLocalDataSource
  @RelaxedMockK lateinit var hiddenShowsLocalDataSource: ArchiveShowsLocalDataSource
  @RelaxedMockK lateinit var showDb: ShowDb

  private lateinit var SUT: MyShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = MyShowsRepository(
      myShowsLocalSource,
      watchlistShowsLocalSource,
      hiddenShowsLocalDataSource,
      transactions,
      mappers
    )
    coEvery { database.myShows } returns myShowsLocalSource
    coEvery { database.watchlistShows } returns watchlistShowsLocalSource
    coEvery { database.archiveShows } returns hiddenShowsLocalDataSource
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(myShowsLocalSource)
    confirmVerified(watchlistShowsLocalSource)
    confirmVerified(hiddenShowsLocalDataSource)
  }

  @Test
  fun `Should load and map single show by Trakt ID`() {
    runBlocking {
      val show = Show.EMPTY.copy(title = "Test")

      coEvery { myShowsLocalSource.getById(any()) } returns showDb
      coEvery { mappers.show.fromDatabase(any()) } returns show

      val testShow = SUT.load(IdTrakt(1L))

      assertThat(testShow?.title).isEqualTo(show.title)
      coVerify(exactly = 1) { myShowsLocalSource.getById(any()) }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map all shows`() {
    runBlocking {
      coEvery { myShowsLocalSource.getAll() } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      SUT.loadAll()

      coVerify(exactly = 1) { myShowsLocalSource.getAll() }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load all shows ids`() {
    runBlocking {
      coEvery { myShowsLocalSource.getAllTraktIds() } returns listOf(1L, 2L)

      val ids = SUT.loadAllIds()

      assertThat(ids).containsExactly(1L, 2L)
      coVerify(exactly = 1) { myShowsLocalSource.getAllTraktIds() }
    }
  }

  @Test
  fun `Should load and map all shows by Trakt Ids`() {
    runBlocking {
      coEvery { myShowsLocalSource.getAll(any()) } returns listOf(showDb, showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      val shows = SUT.loadAll(listOf(IdTrakt(1), IdTrakt(2)))

      assertThat(shows).hasSize(2)
      coVerify(exactly = 1) { myShowsLocalSource.getAll(listOf(1, 2)) }
      coVerify(exactly = 2) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map all recents shows using amount`() {
    runBlocking {
      coEvery { myShowsLocalSource.getAllRecent(any()) } returns listOf(showDb, showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      val shows = SUT.loadAllRecent(2)

      assertThat(shows).hasSize(2)
      coVerify(exactly = 1) { myShowsLocalSource.getAllRecent(2) }
      coVerify(exactly = 2) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should insert show into database using Trakt ID`() {
    runBlocking {
      val slot = slot<List<MyShow>>()
      coJustRun { myShowsLocalSource.insert(capture(slot)) }

      SUT.insert(IdTrakt(10L), 666)

      slot.captured[0].run {
        assertThat(id).isEqualTo(0)
        assertThat(idTrakt).isEqualTo(10)
        assertThat(createdAt).isGreaterThan(0)
        assertThat(updatedAt).isGreaterThan(0)
        assertThat(lastWatchedAt).isEqualTo(666)
      }
      coVerify(exactly = 1) { myShowsLocalSource.insert(any()) }
      coVerify(exactly = 1) { watchlistShowsLocalSource.deleteById(any()) }
      coVerify(exactly = 1) { hiddenShowsLocalDataSource.deleteById(any()) }
    }
  }

  @Test
  fun `Should delete show from database using Trakt ID`() {
    runBlocking {
      val slot = slot<Long>()
      coJustRun { myShowsLocalSource.deleteById(capture(slot)) }

      SUT.delete(IdTrakt(10L))

      assertThat(slot.captured).isEqualTo(10L)
      coVerify(exactly = 1) { myShowsLocalSource.deleteById(10L) }
    }
  }
}
