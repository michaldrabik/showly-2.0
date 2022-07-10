package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_search.BaseMockTest
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.utilities.SearchOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SearchSortingCaseTest : BaseMockTest() {

  private lateinit var SUT: SearchSortingCase

  private val testList = mutableListOf<SearchListItem>()

  @Before
  override fun setUp() {
    super.setUp()
    SUT = SearchSortingCase()

    val element = SearchListItem(
      id = UUID.randomUUID(),
      show = Show.EMPTY,
      movie = Movie.EMPTY,
      image = Image.createUnknown(ImageType.POSTER),
      score = 1F,
    )

    testList.add(element.copy(show = Show.EMPTY.copy(title = "Test2", year = 2000), score = 2F))
    testList.add(element.copy(show = Show.EMPTY.copy(title = "Test1", year = 1000), score = 1F))
    testList.add(element.copy(show = Show.EMPTY.copy(title = "Test3", year = 3000), score = 3F))
  }

  @Test
  fun `Should properly sort ascending by name`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.NAME, sortType = SortType.ASCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test1")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test3")
  }

  @Test
  fun `Should properly sort descending by name`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.NAME, sortType = SortType.DESCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test3")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test1")
  }

  @Test
  fun `Should properly sort ascending by rank`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.RANK, sortType = SortType.ASCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test3")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test1")
  }

  @Test
  fun `Should properly sort descending by rank`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.RANK, sortType = SortType.DESCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test1")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test3")
  }

  @Test
  fun `Should properly sort ascending by release date`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.NEWEST, sortType = SortType.ASCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test1")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test3")
  }

  @Test
  fun `Should properly sort descending by release date`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.NEWEST, sortType = SortType.DESCENDING)

    val result = testList.sortedWith(SUT.sort(options))

    assertThat(result[0].title).isEqualTo("Test3")
    assertThat(result[1].title).isEqualTo("Test2")
    assertThat(result[2].title).isEqualTo("Test1")
  }

  @Test
  fun `Should fail if unsupported sort order`() = runTest {
    val options = SearchOptions(sortOrder = SortOrder.RECENTLY_WATCHED)
    assertThrows(IllegalStateException::class.java) {
      testList.sortedWith(SUT.sort(options))
    }
  }
}
