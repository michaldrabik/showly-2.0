package com.michaldrabik.ui_repository.common

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_repository.R
import com.michaldrabik.ui_repository.mappers.ActorMapper
import com.michaldrabik.ui_repository.mappers.CommentMapper
import com.michaldrabik.ui_repository.mappers.EpisodeMapper
import com.michaldrabik.ui_repository.mappers.IdsMapper
import com.michaldrabik.ui_repository.mappers.ImageMapper
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.mappers.SeasonMapper
import com.michaldrabik.ui_repository.mappers.SettingsMapper
import com.michaldrabik.ui_repository.mappers.ShowMapper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  protected val testDispatcher = TestCoroutineDispatcher()

  @MockK lateinit var database: AppDatabase
  @MockK lateinit var cloud: Cloud

  private val idsMapper = IdsMapper()
  private val episodeMappers = EpisodeMapper(idsMapper)

  @SpyK var mappers = Mappers(
    idsMapper,
    ImageMapper(),
    ShowMapper(idsMapper),
    episodeMappers,
    SeasonMapper(idsMapper, episodeMappers),
    ActorMapper(),
    CommentMapper(),
    SettingsMapper()
  )

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { database.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }
  }
}
