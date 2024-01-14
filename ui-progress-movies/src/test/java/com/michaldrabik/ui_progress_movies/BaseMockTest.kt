package com.michaldrabik.ui_progress_movies

import com.michaldrabik.common_test.MainDispatcherRule
import com.michaldrabik.common_test.UnconfinedCoroutineDispatchers
import io.mockk.MockKAnnotations
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  protected val testDispatchers = UnconfinedCoroutineDispatchers()

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
  }
}
