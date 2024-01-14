package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesPinnedCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var pinnedItemsRepository: PinnedItemsRepository

  private lateinit var SUT: ProgressMoviesPinnedCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = ProgressMoviesPinnedCase(pinnedItemsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should set pinned item properly`() = runBlockingTest {
    SUT.addPinnedItem(Movie.EMPTY)

    coVerify(exactly = 1) { pinnedItemsRepository.addPinnedItem(Movie.EMPTY) }
  }

  @Test
  fun `Should remove pinned item properly`() = runBlockingTest {
    SUT.removePinnedItem(Movie.EMPTY)

    coVerify(exactly = 1) { pinnedItemsRepository.removePinnedItem(Movie.EMPTY) }
  }
}
