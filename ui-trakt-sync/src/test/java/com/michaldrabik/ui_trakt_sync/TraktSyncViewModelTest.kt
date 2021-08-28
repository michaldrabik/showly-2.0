package com.michaldrabik.ui_trakt_sync

import BaseMockTest
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncProgress
import com.michaldrabik.ui_base.events.TraktSyncStart
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.TraktSyncSchedule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.format.DateTimeFormatter

@Suppress("EXPERIMENTAL_API_USAGE")
class TraktSyncViewModelTest : BaseMockTest() {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @RelaxedMockK lateinit var workManager: WorkManager
  @MockK lateinit var miscPreferences: SharedPreferences
  @MockK lateinit var userTraktManager: UserTraktManager
  @MockK lateinit var settingsRepository: SettingsRepository
  @MockK lateinit var dateFormatProvider: DateFormatProvider
  @MockK lateinit var importWatchedRunner: TraktImportWatchedRunner
  @MockK lateinit var importWatchlistRunner: TraktImportWatchlistRunner
  @MockK lateinit var exportWatchedRunner: TraktImportWatchedRunner
  @MockK lateinit var exportWatchlistRunner: TraktExportWatchlistRunner

  private lateinit var SUT: TraktSyncViewModel

  private val stateResult = mutableListOf<TraktSyncUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()
    Dispatchers.setMain(testDispatcher)

    coEvery { importWatchedRunner.isRunning } returns false
    coEvery { importWatchlistRunner.isRunning } returns false
    coEvery { exportWatchedRunner.isRunning } returns false
    coEvery { exportWatchlistRunner.isRunning } returns false
    coEvery { userTraktManager.revokeToken() } just Runs

    SUT = TraktSyncViewModel(
      miscPreferences,
      userTraktManager,
      workManager,
      settingsRepository,
      dateFormatProvider,
      importWatchedRunner,
      importWatchlistRunner,
      exportWatchedRunner,
      exportWatchlistRunner
    )
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  internal fun `Should progress true if any of the runners is active`() = runBlockingTest {
    SUT.viewModelScope.cancel()

    coEvery { importWatchlistRunner.isRunning } returns true

    SUT = TraktSyncViewModel(
      miscPreferences,
      userTraktManager,
      workManager,
      settingsRepository,
      dateFormatProvider,
      importWatchedRunner,
      importWatchlistRunner,
      exportWatchedRunner,
      exportWatchlistRunner
    )

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    assertThat(stateResult.last().isProgress).isTrue()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should invalidate properly`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    coEvery { settingsRepository.load() } returns Settings.createInitial()
    coEvery { userTraktManager.isAuthorized() } returns true
    coEvery { dateFormatProvider.loadFullHourFormat() } returns DateTimeFormatter.ISO_DATE
    coEvery { miscPreferences.getLong(any(), 0) } returns 0

    SUT.invalidate()

    assertThat(stateResult.last().isAuthorized).isTrue()
    assertThat(stateResult.last().traktSyncSchedule).isEqualTo(TraktSyncSchedule.OFF)
    assertThat(stateResult.last().quickSyncEnabled).isEqualTo(false)
    assertThat(stateResult.last().dateFormat).isEqualTo(DateTimeFormatter.ISO_DATE)
    assertThat(stateResult.last().lastTraktSyncTimestamp).isEqualTo(0L)
    coVerify(exactly = 1) { settingsRepository.load() }

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should not authorize trakt if URI is null`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.authorizeTrakt(null)
    coVerify(exactly = 0) { userTraktManager.authorize(any()) }
    assertThat(messagesResult.last().consume()).isEqualTo(R.string.errorAuthorization)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should authorize trakt properly`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    coEvery { settingsRepository.load() } returns Settings.createInitial()
    coEvery { userTraktManager.isAuthorized() } returns true
    coEvery { userTraktManager.authorize(any()) } just Runs
    coEvery { dateFormatProvider.loadFullHourFormat() } returns DateTimeFormatter.ISO_DATE
    coEvery { miscPreferences.getLong(any(), 0) } returns 0

    SUT.authorizeTrakt("testCode")

    assertThat(messagesResult.last().consume()).isEqualTo(R.string.textTraktLoginSuccess)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should save Trakt Sync Schedule`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    coEvery { settingsRepository.load() } returns Settings.createInitial()
    coEvery { settingsRepository.update(any()) } just Runs

    val schedule = TraktSyncSchedule.EVERY_6_HOURS
    SUT.saveTraktSyncSchedule(schedule)

    assertThat(stateResult.last().traktSyncSchedule).isEqualTo(schedule)
    assertThat(messagesResult).isEmpty()
    coVerify(exactly = 1) { settingsRepository.load() }
    coVerify(exactly = 1) { settingsRepository.update(any()) }
    coVerify(exactly = 1) { workManager.cancelUniqueWork(any()) }
    coVerify(exactly = 1) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should handle TraktSyncStart event`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.handleEvent(TraktSyncStart)

    assertThat(stateResult.last().isProgress).isTrue()
    assertThat(stateResult.last().progressStatus).isEqualTo("")
    assertThat(messagesResult.last().consume()).isEqualTo(R.string.textTraktSyncStarted)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should handle TraktSyncProgress event`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.handleEvent(TraktSyncProgress("test"))

    assertThat(stateResult.last().isProgress).isTrue()
    assertThat(stateResult.last().progressStatus).isEqualTo("test")
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should handle TraktSyncSuccess event`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.handleEvent(TraktSyncSuccess)

    assertThat(stateResult.last().isProgress).isFalse()
    assertThat(stateResult.last().progressStatus).isEqualTo("")
    assertThat(messagesResult.last().consume()).isEqualTo(R.string.textTraktSyncComplete)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should handle TraktSyncError event`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.handleEvent(TraktSyncError)

    assertThat(stateResult.last().isProgress).isFalse()
    assertThat(stateResult.last().progressStatus).isEqualTo("")
    assertThat(messagesResult.last().consume()).isEqualTo(R.string.textTraktSyncError)

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should handle TraktSyncAuthError event`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.handleEvent(TraktSyncAuthError)

    assertThat(stateResult.last().isProgress).isFalse()
    assertThat(stateResult.last().progressStatus).isEqualTo("")
    assertThat(stateResult.last().authError).isTrue()
    assertThat(messagesResult.last().consume()).isEqualTo(R.string.errorTraktAuthorization)
    coVerify(exactly = 1) { userTraktManager.revokeToken() }

    job.cancel()
    job2.cancel()
  }
}
