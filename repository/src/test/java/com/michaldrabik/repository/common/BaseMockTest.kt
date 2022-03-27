package com.michaldrabik.repository.common

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.R
import com.michaldrabik.repository.mappers.CommentMapper
import com.michaldrabik.repository.mappers.CustomListMapper
import com.michaldrabik.repository.mappers.EpisodeMapper
import com.michaldrabik.repository.mappers.IdsMapper
import com.michaldrabik.repository.mappers.ImageMapper
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.mappers.MovieMapper
import com.michaldrabik.repository.mappers.NewsMapper
import com.michaldrabik.repository.mappers.PersonMapper
import com.michaldrabik.repository.mappers.RatingsMapper
import com.michaldrabik.repository.mappers.SeasonMapper
import com.michaldrabik.repository.mappers.SettingsMapper
import com.michaldrabik.repository.mappers.ShowMapper
import com.michaldrabik.repository.mappers.StreamingsMapper
import com.michaldrabik.repository.mappers.TranslationMapper
import com.michaldrabik.repository.mappers.UserRatingsMapper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @MockK lateinit var database: LocalDataSource
  @MockK lateinit var transactions: TransactionsProvider
  @MockK lateinit var cloud: RemoteDataSource

  private val idsMapper = IdsMapper()
  private val episodeMappers = EpisodeMapper(idsMapper)

  @SpyK var mappers = Mappers(
    idsMapper,
    ImageMapper(),
    ShowMapper(idsMapper),
    MovieMapper(idsMapper),
    episodeMappers,
    SeasonMapper(idsMapper, episodeMappers),
    PersonMapper(),
    CommentMapper(),
    NewsMapper(),
    SettingsMapper(),
    TranslationMapper(idsMapper),
    CustomListMapper(),
    RatingsMapper(),
    UserRatingsMapper(),
    StreamingsMapper()
  )

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    clearAllMocks()
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { transactions.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }
  }
}
