package com.michaldrabik.ui_search.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class SearchMainCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var showsRepository: ShowsRepository
  @RelaxedMockK lateinit var moviesRepository: MoviesRepository

  private lateinit var SUT: SearchMainCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = SearchMainCase(showsRepository, moviesRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should load my shows ids`() = runBlockingTest {
    SUT.loadMyShowsIds()
    coVerify(exactly = 1) { showsRepository.myShows.loadAllIds() }
  }

  @Test
  fun `Should load shows watchlist ids`() = runBlockingTest {
    SUT.loadWatchlistShowsIds()
    coVerify(exactly = 1) { showsRepository.watchlistShows.loadAllIds() }
  }

  @Test
  fun `Should load my movies ids`() = runBlockingTest {
    SUT.loadMyMoviesIds()
    coVerify(exactly = 1) { moviesRepository.myMovies.loadAllIds() }
  }

  @Test
  fun `Should load movies watchlist ids`() = runBlockingTest {
    SUT.loadWatchlistMoviesIds()
    coVerify(exactly = 1) { moviesRepository.watchlistMovies.loadAllIds() }
  }
}
