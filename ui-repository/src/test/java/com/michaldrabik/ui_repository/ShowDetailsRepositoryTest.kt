package com.michaldrabik.ui_repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.trakt.api.TraktApi
import com.michaldrabik.storage.database.dao.ShowsDao
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.User
import com.michaldrabik.ui_repository.common.BaseMockTest
import com.michaldrabik.ui_repository.shows.ShowDetailsRepository
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import com.michaldrabik.network.trakt.model.Comment as CommentNetwork
import com.michaldrabik.network.trakt.model.Show as ShowRemote
import com.michaldrabik.network.trakt.model.User as UserNetwork

class ShowDetailsRepositoryTest : BaseMockTest() {

  @MockK lateinit var traktApi: TraktApi
  @MockK lateinit var showsDao: ShowsDao

  private lateinit var SUT: ShowDetailsRepository

  @Before
  override fun setUp() {
    super.setUp()
    every { database.showsDao() } returns showsDao
    every { cloud.traktApi } returns traktApi

    SUT = ShowDetailsRepository(cloud, database, mappers)
  }

  @Test
  fun `Should load cached show details on given conditions`() {
    runBlocking {
      val showDb = mockk<Show>(relaxed = true) {
        every { idTrakt } returns 1
        every { updatedAt } returns nowUtcMillis() - 100
      }
      coEvery { showsDao.getById(any()) } returns showDb

      val show = SUT.load(IdTrakt(1), false)

      assertThat(show.ids.trakt).isEqualTo(IdTrakt(1))
      coVerifySequence {
        showsDao.getById(any())
        traktApi.fetchShow(any()) wasNot Called
      }
    }
  }

  @Test
  fun `Should load remote show details if force flag is set`() {
    runBlocking {
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.trakt } returns 1
      }
      coEvery { showsDao.getById(any()) } returns null
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { traktApi.fetchShow(any()) } returns showRemote

      val show = SUT.load(IdTrakt(1), true)

      assertThat(show.ids.trakt).isEqualTo(IdTrakt(1))

      coVerifySequence {
        showsDao.getById(any())
        traktApi.fetchShow(any())
        showsDao.upsert(any())
      }
    }
  }

  @Test
  fun `Should load remote show details if nothing is cached`() {
    runBlocking {
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.trakt } returns 1
      }
      coEvery { showsDao.getById(any()) } returns null
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { traktApi.fetchShow(any()) } returns showRemote

      val show = SUT.load(IdTrakt(1), false)

      assertThat(show.ids.trakt).isEqualTo(IdTrakt(1))

      coVerifySequence {
        showsDao.getById(any())
        traktApi.fetchShow(any())
        showsDao.upsert(any())
      }
    }
  }

  @Test
  fun `Should load remote show details if cached show expired`() {
    runBlocking {
      val showDb = mockk<Show>(relaxed = true) {
        every { idTrakt } returns 1
        every { updatedAt } returns nowUtcMillis() - TimeUnit.DAYS.toMillis(10)
      }
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.trakt } returns 1
      }
      coEvery { showsDao.getById(any()) } returns showDb
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { traktApi.fetchShow(any()) } returns showRemote

      val show = SUT.load(IdTrakt(1), false)

      assertThat(show.ids.trakt).isEqualTo(IdTrakt(1))

      coVerifySequence {
        showsDao.getById(any())
        traktApi.fetchShow(any())
        showsDao.upsert(any())
      }
    }
  }

  @Test
  fun `Should load comments with given limit`() {
    runBlocking {
      val commentNetwork = CommentNetwork(1, 2, "", 1, true, true, null, UserNetwork("", ""))
      val comment = Comment(1, 2, "", 1, true, true, null, User("", ""))
      coEvery { traktApi.fetchShowComments(any(), any()) } returns listOf(commentNetwork)

      val comments = SUT.loadComments(IdTrakt(1), 10)

      assertThat(comments).containsExactly(comment)
      coVerify { traktApi.fetchShowComments(1, 10) }
      confirmVerified(traktApi)
    }
  }
}
