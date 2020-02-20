import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.mappers.ActorMapper
import com.michaldrabik.showly2.model.mappers.EpisodeMapper
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
  @SpyK var mappers = Mappers(
    ImageMapper(),
    ShowMapper(),
    EpisodeMapper(),
    SeasonMapper(EpisodeMapper()),
    ActorMapper(),
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
