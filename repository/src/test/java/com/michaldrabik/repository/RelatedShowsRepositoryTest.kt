package com.michaldrabik.repository

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.dao.RelatedShowsDao
import com.michaldrabik.data_local.database.dao.ShowsDao
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.shows.RelatedShowsRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit.HOURS

class RelatedShowsRepositoryTest : BaseMockTest() {

  @MockK
  lateinit var traktApi: TraktApi

  @RelaxedMockK
  lateinit var relatedShowsDao: RelatedShowsDao

  @MockK
  lateinit var showsDao: ShowsDao

  private lateinit var SUT: RelatedShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    every { database.showsDao() } returns showsDao
    every { database.relatedShowsDao() } returns relatedShowsDao
    every { cloud.traktApi } returns traktApi

    SUT = RelatedShowsRepository(cloud, database, mappers)
  }

  @Test
  fun `Should return cached shows properly`() {
    runBlocking {
      val showDb = mockk<RelatedShow>(relaxed = true) {
        every { updatedAt } returns nowUtcMillis() - HOURS.toMillis(1)
      }
      coEvery { showsDao.getAll(any()) } returns emptyList()
      coEvery { relatedShowsDao.getAllById(any()) } returns listOf(showDb)

      SUT.loadAll(mockk(relaxed = true))

      coVerifySequence {
        relatedShowsDao.getAllById(any())
        showsDao.getAll(any())
      }
      coVerify(exactly = 0) { traktApi.fetchRelatedShows(any()) }
    }
  }

  @Test
  fun `Should return remote shows if nothing is cached`() {
    runBlocking {
      coEvery { showsDao.getAll(any()) } returns emptyList()
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { traktApi.fetchRelatedShows(any()) } returns listOf(mockk(relaxed = true))
      coEvery { relatedShowsDao.getAllById(any()) } returns listOf()

      SUT.loadAll(mockk(relaxed = true))

      coVerifyOrder {
        relatedShowsDao.getAllById(any())
        traktApi.fetchRelatedShows(any())
      }
      coVerify(exactly = 0) { showsDao.getAll(any()) }
    }
  }

  @Test
  fun `Should return remote shows if cached values expired`() {
    runBlocking {
      val showDb = mockk<RelatedShow>(relaxed = true) {
        every { updatedAt } returns nowUtcMillis() - (Config.RELATED_CACHE_DURATION + 1000)
      }
      coEvery { showsDao.getAll(any()) } returns emptyList()
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { traktApi.fetchRelatedShows(any()) } returns listOf(mockk(relaxed = true))
      coEvery { relatedShowsDao.getAllById(any()) } returns listOf(showDb)

      SUT.loadAll(mockk(relaxed = true))

      coVerifyOrder {
        relatedShowsDao.getAllById(any())
        traktApi.fetchRelatedShows(any())
      }
      coVerify(exactly = 0) { showsDao.getAll(any()) }
    }
  }
}
