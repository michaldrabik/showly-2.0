package com.michaldrabik.ui_progress_movies.progress.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.helpers.ProgressMoviesItemsSorter
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesItemsCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var sorter: ProgressMoviesItemsSorter
  @RelaxedMockK lateinit var moviesRepository: MoviesRepository
  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository
  @RelaxedMockK lateinit var ratingsRepository: RatingsRepository
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var imagesProvider: MovieImagesProvider
  @RelaxedMockK lateinit var pinnedItemsRepository: PinnedItemsRepository
  @RelaxedMockK lateinit var dateFormatProvider: DateFormatProvider

  private lateinit var SUT: ProgressMoviesItemsCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { translationsRepository.getLanguage() } returns "en"
    coEvery { dateFormatProvider.loadFullDayFormat() } returns DateTimeFormatter.ofPattern("dd MMM yyyy")

    coEvery { settingsRepository.sorting getProperty "progressMoviesSortOrder" } returns SortOrder.RANK
    coEvery { settingsRepository.sorting getProperty "progressMoviesSortType" } returns SortType.DESCENDING

    coEvery { imagesProvider.findCachedImage(any(), any()) } returns Image.createUnknown(ImageType.POSTER)
    coEvery { pinnedItemsRepository.isItemPinned(any<Movie>()) } returns false

    SUT = ProgressMoviesItemsCase(
      moviesRepository,
      translationsRepository,
      ratingsRepository,
      settingsRepository,
      pinnedItemsRepository,
      imagesProvider,
      dateFormatProvider,
      sorter
    )
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should load items properly`() = runBlocking {
    val movie = Movie.EMPTY.copy(
      title = "test1",
      released = LocalDate.now().minusYears(5)
    )

    coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie)

    val result = SUT.loadItems(searchQuery = "")

    assertThat(result).isNotEmpty()
    assertThat(result).hasSize(2)
  }

  @Test
  fun `Should filter items by query if present`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        released = LocalDate.now().minusYears(5)
      )

      val movie2 = Movie.EMPTY.copy(
        title = "xxx",
        released = LocalDate.now().minusYears(5)
      )

      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1, movie2)

      val result = SUT.loadItems(searchQuery = "test")

      assertThat(result).hasSize(2)
      assertThat(result[1].movie).isEqualTo(movie1)
    }
  }

  @Test
  fun `Should put pinned items at the top`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        released = LocalDate.now().minusDays(10)
      )

      val movie2 = Movie.EMPTY.copy(
        title = "xxx",
        released = LocalDate.now().minusDays(10)
      )

      val movie3 = Movie.EMPTY.copy(
        title = "xxx2",
        released = LocalDate.now().minusDays(10)
      )

      coEvery { pinnedItemsRepository.isItemPinned(movie2) } returns true
      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1, movie2, movie3)

      val result = SUT.loadItems(searchQuery = "")

      assertThat(result).isNotEmpty()
      assertThat((result[1] as ProgressMovieListItem.MovieItem).isPinned).isTrue()
      assertThat(result[1].movie).isEqualTo(movie2)
      assertThat((result[2] as ProgressMovieListItem.MovieItem).isPinned).isFalse()
      assertThat((result[3] as ProgressMovieListItem.MovieItem).isPinned).isFalse()
    }
  }

  @Test
  fun `Should sort items properly with given comparator`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        rating = 4F,
        released = LocalDate.now().minusDays(10)
      )

      val movie2 = Movie.EMPTY.copy(
        title = "xxx",
        rating = 1F,
        released = LocalDate.now().minusDays(10)
      )

      val movie3 = Movie.EMPTY.copy(
        title = "xxx2",
        rating = 11F,
        released = LocalDate.now().minusDays(10)
      )

      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1, movie2, movie3)
      coEvery { sorter.sort(any(), any()) } returns compareByDescending { it.movie.rating }

      val result1 = SUT.loadItems(searchQuery = "")
      assertThat(result1[1].movie).isEqualTo(movie3)
      assertThat(result1[2].movie).isEqualTo(movie1)
      assertThat(result1[3].movie).isEqualTo(movie2)

      coEvery { sorter.sort(any(), any()) } returns compareBy { it.movie.title }

      val result2 = SUT.loadItems(searchQuery = "")
      assertThat(result2[1].movie).isEqualTo(movie1)
      assertThat(result2[2].movie).isEqualTo(movie2)
      assertThat(result2[3].movie).isEqualTo(movie3)
    }
  }

  @Test
  fun `Should filter items with no release date and upcoming ones`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        released = LocalDate.now().plusDays(10)
      )

      val movie2 = Movie.EMPTY.copy(
        title = "xxx",
        released = null
      )

      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1, movie2)

      val result = SUT.loadItems(searchQuery = "test")

      assertThat(result).isEmpty()
    }
  }

  @Test
  fun `Should not add translation if default language`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        released = LocalDate.now().minusYears(5)
      )

      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1)

      val result = SUT.loadItems(searchQuery = "")

      assertThat(result).hasSize(2)
      assertThat(result[1].movie).isEqualTo(movie1)
      assertThat((result[1] as ProgressMovieListItem.MovieItem).translation).isNull()
    }
  }

  @Test
  fun `Should add translation if not default langiage`() {
    runBlocking {
      val movie1 = Movie.EMPTY.copy(
        title = "test1",
        released = LocalDate.now().minusYears(5)
      )

      coEvery { translationsRepository.getLanguage() } returns "pl"
      coEvery { translationsRepository.loadTranslation(any<Movie>(), any(), any()) } returns Translation.EMPTY
      coEvery { moviesRepository.watchlistMovies.loadAll() } returns listOf(movie1)

      val result = SUT.loadItems(searchQuery = "")

      assertThat(result).hasSize(2)
      assertThat(result[1].movie).isEqualTo(movie1)
      assertThat((result[1] as ProgressMovieListItem.MovieItem).translation).isEqualTo(Translation.EMPTY)
    }
  }
}
