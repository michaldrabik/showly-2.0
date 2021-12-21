package com.michaldrabik.ui_progress_movies.calendar.helpers.filters

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class CalendarRecentsFilterTest : BaseMockTest() {

  private lateinit var SUT: CalendarRecentsFilter

  @Before
  override fun setUp() {
    super.setUp()
    SUT = CalendarRecentsFilter()
  }

  @Test
  fun `Should return true if release date is before now`() = runBlockingTest {
    val movie = Movie.EMPTY.copy(released = LocalDate.now().minusDays(1))
    val result = SUT.filter(ZonedDateTime.now(), movie)
    assertThat(result).isTrue()
  }

  @Test
  fun `Should return false if release date is null`() = runBlockingTest {
    val movie = Movie.EMPTY.copy(released = null)
    val result = SUT.filter(ZonedDateTime.now(), movie)
    assertThat(result).isFalse()
  }

  @Test
  fun `Should return false if release date is today`() = runBlockingTest {
    val movie = Movie.EMPTY.copy(released = LocalDate.now())
    val result = SUT.filter(ZonedDateTime.now(), movie)
    assertThat(result).isFalse()
  }
}
