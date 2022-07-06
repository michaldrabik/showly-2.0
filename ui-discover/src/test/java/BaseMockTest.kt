import io.mockk.MockKAnnotations
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
  }
}
