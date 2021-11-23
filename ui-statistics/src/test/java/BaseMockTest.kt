import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  protected val testDispatcher = TestCoroutineDispatcher()

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
  }
}
