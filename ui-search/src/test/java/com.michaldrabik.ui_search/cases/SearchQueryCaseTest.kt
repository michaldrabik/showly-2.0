package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.data_remote.trakt.model.SearchResult
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class SearchQueryCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var cloud: Cloud
  @RelaxedMockK lateinit var traktApi: TraktApi
  @RelaxedMockK lateinit var mappers: Mappers
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: SearchQueryCase

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { cloud.traktApi } returns traktApi
    coEvery { settingsRepository.isMoviesEnabled } returns true
    SUT = SearchQueryCase(cloud, mappers, settingsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should run search query and return results sorted by score`() = runBlockingTest {
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
  }
}
