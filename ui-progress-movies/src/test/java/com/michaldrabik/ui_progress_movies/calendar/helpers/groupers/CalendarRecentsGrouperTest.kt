package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class CalendarRecentsGrouperTest : BaseMockTest() {

  private lateinit var SUT: CalendarRecentsGrouper

  @Before
  override fun setUp() {
    super.setUp()
    SUT = CalendarRecentsGrouper()
  }

  @Test
  fun `Should group past items by time properly`() = runBlockingTest {
    val zonedNow = ZonedDateTime.parse("2021-12-20T18:00:00Z")
    val now = LocalDate.parse("2021-12-20") // Monday
    val movie1 = Movie.EMPTY.copy(released = now.minusDays(1))
    val movie2 = Movie.EMPTY.copy(released = now.minusDays(7))
    val movie3 = Movie.EMPTY.copy(released = now.minusDays(30))
    val movie4 = Movie.EMPTY.copy(released = now.minusDays(90))

    val item1 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns movie1
    }
    val item2 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns movie2
    }
    val item3 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns movie3
    }
    val item4 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns movie4
    }

    val results = SUT.groupByTime(zonedNow, listOf(item1, item2, item3, item4))

    assertThat(results).hasSize(8)
    assertThat((results[0] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textYesterday)
    assertThat((results[1] as CalendarMovieListItem.MovieItem).movie).isEqualTo(movie1)
    assertThat((results[2] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textLast7Days)
    assertThat((results[3] as CalendarMovieListItem.MovieItem).movie).isEqualTo(movie2)
    assertThat((results[4] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textLast30Days)
    assertThat((results[5] as CalendarMovieListItem.MovieItem).movie).isEqualTo(movie3)
    assertThat((results[6] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textLast90Days)
    assertThat((results[7] as CalendarMovieListItem.MovieItem).movie).isEqualTo(movie4)
  }

  @Test
  fun `Should not include items older than 90 days`() = runBlockingTest {
    val zonedNow = ZonedDateTime.parse("2021-12-20T18:00:00Z")
    val now = LocalDate.parse("2021-12-20") // Monday
    val movie1 = Movie.EMPTY.copy(released = now.minusDays(91))

    val item1 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns movie1
    }

    val results = SUT.groupByTime(zonedNow, listOf(item1))

    assertThat(results).isEmpty()
  }
}
