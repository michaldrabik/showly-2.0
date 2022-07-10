package com.michaldrabik.ui_search

import com.michaldrabik.ui_search.helpers.MainDispatcherRule
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Rule

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
  }
}
