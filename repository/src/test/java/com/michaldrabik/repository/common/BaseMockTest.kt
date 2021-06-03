package com.michaldrabik.repository.common

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.R
import com.michaldrabik.repository.mappers.ActorMapper
import com.michaldrabik.repository.mappers.CommentMapper
import com.michaldrabik.repository.mappers.CustomListMapper
import com.michaldrabik.repository.mappers.EpisodeMapper
import com.michaldrabik.repository.mappers.IdsMapper
import com.michaldrabik.repository.mappers.ImageMapper
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.mappers.MovieMapper
import com.michaldrabik.repository.mappers.NewsMapper
import com.michaldrabik.repository.mappers.RatingsMapper
import com.michaldrabik.repository.mappers.SeasonMapper
import com.michaldrabik.repository.mappers.SettingsMapper
import com.michaldrabik.repository.mappers.ShowMapper
import com.michaldrabik.repository.mappers.StreamingsMapper
import com.michaldrabik.repository.mappers.TranslationMapper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @MockK lateinit var database: AppDatabase
  @MockK lateinit var cloud: Cloud

  private val idsMapper = IdsMapper()
  private val episodeMappers = EpisodeMapper(idsMapper)

  @SpyK var mappers = Mappers(
    idsMapper,
    ImageMapper(),
    ShowMapper(idsMapper),
    MovieMapper(idsMapper),
    episodeMappers,
    SeasonMapper(idsMapper, episodeMappers),
    ActorMapper(),
    CommentMapper(),
    NewsMapper(),
    SettingsMapper(),
    TranslationMapper(idsMapper),
    CustomListMapper(),
    RatingsMapper(),
    StreamingsMapper()
  )

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { database.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }
  }
}
