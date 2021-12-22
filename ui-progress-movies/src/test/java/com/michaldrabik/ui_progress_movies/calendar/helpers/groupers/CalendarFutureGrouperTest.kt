package com.michaldrabik.ui_progress_movies.calendar.helpers.groupers

import com.google.common.truth.Truth.assertThat
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
class CalendarFutureGrouperTest : BaseMockTest() {

  private lateinit var SUT: CalendarFutureGrouper

  @Before
  override fun setUp() {
    super.setUp()
    SUT = CalendarFutureGrouper()
  }

  @Test
  fun `Should group past items by time properly`() = runBlockingTest {
    val zonedNow = ZonedDateTime.parse("2021-10-11T12:00:00Z")
    val now = LocalDate.parse("2021-10-11") // Monday
    val item1 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now
      }
    }
    val item2 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(1)
      }
    }
    val item3 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(6)
      }
    }
    val item4 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(7)
      }
    }
    val item5 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(14)
      }
    }
    val item6 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(21)
      }
    }
    val item7 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(60)
      }
    }
    val item8 = mockk<CalendarMovieListItem.MovieItem> {
      every { movie } returns mockk {
        every { released } returns now.plusDays(100)
      }
    }

    val results = SUT.groupByTime(
      zonedNow,
      listOf(item1, item2, item3, item4, item5, item6, item7, item8)
    )

    assertThat(results).hasSize(16)
    assertThat((results[0] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textToday)
    assertThat((results[1] as CalendarMovieListItem.MovieItem)).isEqualTo(item1)
    assertThat((results[2] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textTomorrow)
    assertThat((results[3] as CalendarMovieListItem.MovieItem)).isEqualTo(item2)
    assertThat((results[4] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textThisWeek)
    assertThat((results[5] as CalendarMovieListItem.MovieItem)).isEqualTo(item3)
    assertThat((results[6] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textNextWeek)
    assertThat((results[7] as CalendarMovieListItem.MovieItem)).isEqualTo(item4)
    assertThat((results[8] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textThisMonth)
    assertThat((results[9] as CalendarMovieListItem.MovieItem)).isEqualTo(item5)
    assertThat((results[10] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textNextMonth)
    assertThat((results[11] as CalendarMovieListItem.MovieItem)).isEqualTo(item6)
    assertThat((results[12] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textThisYear)
    assertThat((results[13] as CalendarMovieListItem.MovieItem)).isEqualTo(item7)
    assertThat((results[14] as CalendarMovieListItem.Header).textResId).isEqualTo(R.string.textLater)
    assertThat((results[15] as CalendarMovieListItem.MovieItem)).isEqualTo(item8)
  }
}
