package com.michaldrabik.ui_progress_movies.calendar.helpers.sorter

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@Suppress("EXPERIMENTAL_API_USAGE")
class CalendarRecentsSorterTest : BaseMockTest() {

  private lateinit var SUT: CalendarRecentsSorter

  @Before
  override fun setUp() {
    super.setUp()
    SUT = CalendarRecentsSorter()
  }

  @Test
  fun `Should sort by release date`() = runBlockingTest {
    val movie1 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(333))
    val movie2 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(33))
    val movie3 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(3))

    val result = listOf(movie2, movie1, movie3).sortedWith(SUT.sort())

    assertThat(result).containsExactly(movie1, movie2, movie3)
  }

  @Test
  fun `Should sort by year if release date is the same`() = runBlockingTest {
    val movie1 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(3), year = 333)
    val movie2 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(3), year = 33)
    val movie3 = Movie.EMPTY.copy(released = LocalDate.now().plusDays(3), year = 3)

    val result = listOf(movie2, movie1, movie3).sortedWith(SUT.sort())

    assertThat(result).containsExactly(movie1, movie2, movie3)
  }
}
