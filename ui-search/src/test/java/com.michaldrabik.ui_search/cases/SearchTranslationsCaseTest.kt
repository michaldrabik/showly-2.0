package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTranslationsCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository

  private lateinit var SUT: SearchTranslationsCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = SearchTranslationsCase(translationsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should return empty show translation if language is default`() = runTest {
    coEvery { translationsRepository.getLanguage() } returns "en"

    val item = mockk<Show>()
    val result = SUT.loadTranslation(item)

    assertThat(result).isEqualTo(Translation.EMPTY)
    coVerify(exactly = 1) { translationsRepository.getLanguage() }
    confirmVerified(translationsRepository)
  }

  @Test
  fun `Should return empty movie translation if language is default`() = runTest {
    coEvery { translationsRepository.getLanguage() } returns "en"

    val item = mockk<Movie>()
    val result = SUT.loadTranslation(item)

    assertThat(result).isEqualTo(Translation.EMPTY)
    coVerify(exactly = 1) { translationsRepository.getLanguage() }
    confirmVerified(translationsRepository)
  }

  @Test
  fun `Should return show translation if language is not default`() = runTest {
    coEvery { translationsRepository.getLanguage() } returns "pl"
    coEvery { translationsRepository.loadTranslation(any<Show>(), any(), any()) } returns Translation.EMPTY

    val item = mockk<Show>()
    val result = SUT.loadTranslation(item)

    assertThat(result).isNotNull()
    coVerify(exactly = 1) { translationsRepository.getLanguage() }
    coVerify(exactly = 1) { translationsRepository.loadTranslation(any<Show>(), any(), any()) }
    confirmVerified(translationsRepository)
  }

  @Test
  fun `Should return movie translation if language is not default`() = runTest {
    coEvery { translationsRepository.getLanguage() } returns "pl"
    coEvery { translationsRepository.loadTranslation(any<Movie>(), any(), any()) } returns Translation.EMPTY

    val item = mockk<Movie>()
    val result = SUT.loadTranslation(item)

    assertThat(result).isNotNull()
    coVerify(exactly = 1) { translationsRepository.getLanguage() }
    coVerify(exactly = 1) { translationsRepository.loadTranslation(any<Movie>(), any(), any()) }
    confirmVerified(translationsRepository)
  }
}
