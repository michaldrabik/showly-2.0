package com.michaldrabik.ui_statistics_movies

import BaseMockTest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_statistics_movies.cases.StatisticsMoviesLoadRatingsCase
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class StatisticsMoviesViewModelTest : BaseMockTest() {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @MockK lateinit var ratingsCase: StatisticsMoviesLoadRatingsCase
  @MockK lateinit var moviesRepository: MoviesRepository

  private lateinit var SUT: StatisticsMoviesViewModel

  private val stateResult = mutableListOf<StatisticsMoviesUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()
    Dispatchers.setMain(testDispatcher)

    SUT = StatisticsMoviesViewModel(
      ratingsCase,
      moviesRepository
    )
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  internal fun `Should load ratings`() = runBlockingTest {
    val movieItem = StatisticsMoviesRatingItem(Movie.EMPTY, Image.createUnknown(ImageType.POSTER), false, TraktRating.EMPTY)
    coEvery { ratingsCase.loadRatings() } returns listOf(movieItem)

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.loadRatings()

    assertThat(stateResult.last().ratings?.size).isEqualTo(1)
    assertThat(stateResult.last().ratings).contains(movieItem)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should load empty ratings in case of error`() = runBlockingTest {
    coEvery { ratingsCase.loadRatings() } throws Throwable("Test error")

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.loadRatings()

    assertThat(stateResult.last().ratings).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should load statistics properly`() = runBlockingTest {
    val movies = listOf(
      Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(1)), runtime = 1, genres = listOf("war", "drama")),
      Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(2)), runtime = 2, genres = listOf("war", "animation")),
      Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(3)), runtime = 3, genres = listOf("war", "animation")),
    )

    coEvery { moviesRepository.myMovies.loadAll() } returns movies

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.loadData(initialDelay = 0)

    val result = stateResult.last()
    assertThat(result.totalWatchedMovies).isEqualTo(3)
    assertThat(result.totalTimeSpentMinutes).isEqualTo(6)
    assertThat(result.topGenres?.size).isEqualTo(3)
    assertThat(result.topGenres).containsExactly(Genre.WAR, Genre.ANIMATION, Genre.DRAMA)

    job.cancel()
    job2.cancel()
  }
}