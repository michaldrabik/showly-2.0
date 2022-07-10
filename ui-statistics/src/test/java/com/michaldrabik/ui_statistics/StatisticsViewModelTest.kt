package com.michaldrabik.ui_statistics

import BaseMockTest
import TestData
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_statistics.cases.StatisticsLoadRatingsCase
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class StatisticsViewModelTest : BaseMockTest() {

  @MockK lateinit var ratingsCase: StatisticsLoadRatingsCase
  @MockK lateinit var showsRepository: ShowsRepository
  @MockK lateinit var translationsRepository: TranslationsRepository
  @MockK lateinit var imagesProvider: ShowImagesProvider
  @RelaxedMockK lateinit var database: LocalDataSource
  @RelaxedMockK lateinit var mappers: Mappers

  private lateinit var SUT: StatisticsViewModel

  private val stateResult = mutableListOf<StatisticsUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { translationsRepository.getLanguage() } returns "en"
    coEvery { imagesProvider.findCachedImage(any(), any()) } returns Image.createAvailable(
      Ids.EMPTY,
      ImageType.POSTER,
      ImageFamily.SHOW,
      "",
      ImageSource.TMDB
    )

    SUT = StatisticsViewModel(
      ratingsCase,
      showsRepository,
      translationsRepository,
      imagesProvider,
      database,
      mappers
    )
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
  }

  @Test
  internal fun `Should load ratings`() = runTest {
    val movieItem = StatisticsRatingItem(Show.EMPTY, Image.createUnknown(ImageType.POSTER), false, TraktRating.EMPTY)
    coEvery { ratingsCase.loadRatings() } returns listOf(movieItem)

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadRatings()

    assertThat(stateResult.last().ratings?.size).isEqualTo(1)
    assertThat(stateResult.last().ratings).contains(movieItem)

    job.cancel()
  }

  @Test
  internal fun `Should load empty ratings in case of error`() = runTest {
    coEvery { ratingsCase.loadRatings() } throws Throwable("Test error")

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadRatings()

    assertThat(stateResult.last().ratings).isEmpty()

    job.cancel()
  }

  @Test
  internal fun `Should load statistics properly`() = runTest {
    val shows = listOf(
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(1)), runtime = 1, genres = listOf("war", "drama")),
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(2)), runtime = 2, genres = listOf("war", "animation")),
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(3)), runtime = 3, genres = listOf("war", "animation")),
    )

    val shows2 = listOf(
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(4)), runtime = 1, genres = listOf("war", "drama")),
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(5)), runtime = 2, genres = listOf("war", "animation")),
      Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = IdTrakt(6)), runtime = 3, genres = listOf("war", "animation")),
    )

    coEvery { showsRepository.myShows.loadAll() } returns shows
    coEvery { showsRepository.hiddenShows.loadAll() } returns shows2

    coEvery { database.episodes.getAllWatchedForShows(any()) } returns listOf(
      TestData.createEpisode().copy(idShowTrakt = 1, runtime = 5),
      TestData.createEpisode().copy(idShowTrakt = 2, runtime = 6),
      TestData.createEpisode().copy(idShowTrakt = 3, runtime = 7),
      TestData.createEpisode().copy(idShowTrakt = 3, runtime = 7),
    )

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadData(limit = 0, initialDelay = 0)

    val result = stateResult.last()
    assertThat(result.mostWatchedShows).hasSize(5)
    assertThat(result.mostWatchedTotalCount).isEqualTo(6)
    assertThat(result.totalTimeSpentMinutes).isEqualTo(25)
    assertThat(result.totalWatchedEpisodes).isEqualTo(4)
    assertThat(result.totalWatchedEpisodesShows).isEqualTo(3)
    assertThat(result.topGenres?.size).isEqualTo(3)
    assertThat(result.topGenres).containsExactly(Genre.WAR, Genre.ANIMATION, Genre.DRAMA)

    job.cancel()
  }
}
