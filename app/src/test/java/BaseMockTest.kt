import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.mappers.ActorMapper
import com.michaldrabik.showly2.model.mappers.CommentMapper
import com.michaldrabik.showly2.model.mappers.EpisodeMapper
import com.michaldrabik.showly2.model.mappers.IdsMapper
import com.michaldrabik.showly2.model.mappers.ImageMapper
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.model.mappers.SeasonMapper
import com.michaldrabik.showly2.model.mappers.SettingsMapper
import com.michaldrabik.showly2.model.mappers.ShowMapper
import com.michaldrabik.storage.database.AppDatabase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Before

abstract class BaseMockTest {

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
