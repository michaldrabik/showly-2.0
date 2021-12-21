package com.michaldrabik.ui_progress_movies

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @RelaxedMockK lateinit var database: AppDatabase

  protected val testDispatcher = TestCoroutineDispatcher()

  @Before
  open fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { database.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }
  }
}
