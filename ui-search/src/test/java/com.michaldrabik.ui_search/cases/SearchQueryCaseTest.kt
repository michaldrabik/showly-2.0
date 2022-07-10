package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SearchResult
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class SearchQueryCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var cloud: RemoteDataSource
  @RelaxedMockK lateinit var traktApi: TraktRemoteDataSource
  @RelaxedMockK lateinit var mappers: Mappers
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var showsRepository: ShowsRepository
  @RelaxedMockK lateinit var moviesRepository: MoviesRepository
  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository
  @RelaxedMockK lateinit var showImagesProvider: ShowImagesProvider
  @RelaxedMockK lateinit var movieImagesProvider: MovieImagesProvider

  private lateinit var SUT: SearchQueryCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { cloud.trakt } returns traktApi
    coEvery { settingsRepository.isMoviesEnabled } returns true
    coEvery { translationsRepository.getLanguage() } returns "en"

    coEvery { showImagesProvider.findCachedImage(any(), any()) } returns Image.createUnknown(ImageType.POSTER)
    coEvery { movieImagesProvider.findCachedImage(any(), any()) } returns Image.createUnknown(ImageType.POSTER)

    coEvery { showsRepository.myShows.loadAllIds() } returns emptyList()
    coEvery { showsRepository.watchlistShows.loadAllIds() } returns emptyList()
    coEvery { moviesRepository.myMovies.loadAllIds() } returns emptyList()
    coEvery { moviesRepository.watchlistMovies.loadAllIds() } returns emptyList()

    SUT = SearchQueryCase(
      cloud,
      mappers,
      settingsRepository,
      showsRepository,
      moviesRepository,
      translationsRepository,
      showImagesProvider,
      movieImagesProvider
    )
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should run search query and return results sorted by score`() = runTest {
    val show = mockk<Show> {
      coEvery { votes } returnsMany listOf(10, 20, 30)
    }
    val item1 = SearchResult(score = 1F, show = show, movie = null, person = null)
    val item2 = SearchResult(score = 2F, show = show, movie = null, person = null)
    val item3 = SearchResult(score = 3F, show = show, movie = null, person = null)

    coEvery { traktApi.fetchSearch(any(), any()) } returns listOf(item1, item2, item3)

    val result = SUT.searchByQuery("test")

    assertThat(result).hasSize(3)
    assertThat(result[0].score).isEqualTo(3F)
    assertThat(result[1].score).isEqualTo(2F)
    assertThat(result[2].score).isEqualTo(1F)
    coVerify(exactly = 1) { traktApi.fetchSearch("test", any()) }
    coVerify(exactly = 3) { showImagesProvider.findCachedImage(any(), any()) }
    coVerify(exactly = 0) { movieImagesProvider.findCachedImage(any(), any()) }
  }
}
