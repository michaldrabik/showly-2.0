package com.michaldrabik.showly2.ui.main.cases

import BaseMockTest
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.showly2.ui.main.cases.MainRateAppCase.Companion.KEY_RATE_APP_COUNT
import com.michaldrabik.showly2.ui.main.cases.MainRateAppCase.Companion.KEY_RATE_APP_TIMESTAMP
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class MainRateAppCaseTest : BaseMockTest() {

  @MockK
  lateinit var sharedPreferences: SharedPreferences

  @MockK
  lateinit var sharedPreferencesEditor: SharedPreferences.Editor

  private lateinit var SUT: MainRateAppCase

  @Before
  override fun setUp() {
    super.setUp()
    every { sharedPreferencesEditor.apply() } just Runs
    every { sharedPreferencesEditor.putInt(any(), any()) } returns sharedPreferencesEditor
    every { sharedPreferencesEditor.putLong(any(), any()) } returns sharedPreferencesEditor
    every { sharedPreferences.edit() } returns sharedPreferencesEditor

    SUT = MainRateAppCase(sharedPreferences)
  }

  @Test
  fun `Should return false if check is initial`() {
    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 0
    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns -1L

    val result = SUT.shouldShowRateApp()

    assertThat(result).isFalse()
    verify { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 0) }
    verify { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
    verify { sharedPreferencesEditor.apply() }
    confirmVerified(sharedPreferencesEditor)
  }

  @Test
  fun `Should return false if not enough days have passed`() {
    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 1
    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns (nowUtcMillis() - TimeUnit.DAYS.toMillis(5))

    val result = SUT.shouldShowRateApp()

    assertThat(result).isFalse()
    verify(exactly = 0) { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 2) }
    verify(exactly = 0) { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
    verify(exactly = 0) { sharedPreferencesEditor.apply() }

    confirmVerified(sharedPreferencesEditor)
  }

  @Test
  fun `Should return false if not enough days have passed before last reminder`() {
    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 2
    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns (nowUtcMillis() - TimeUnit.DAYS.toMillis(12))

    val result = SUT.shouldShowRateApp()

    assertThat(result).isFalse()
    verify(exactly = 0) { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 3) }
    verify(exactly = 0) { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
    verify(exactly = 0) { sharedPreferencesEditor.apply() }

    confirmVerified(sharedPreferencesEditor)
  }

  @Test
  fun `Should return true if enough days have passed before another reminder`() {
    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 2
    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns (nowUtcMillis() - TimeUnit.DAYS.toMillis(15))

    val result = SUT.shouldShowRateApp()

    assertThat(result).isTrue()
    verify(exactly = 1) { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 2) }
    verify(exactly = 1) { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
    verify(exactly = 1) { sharedPreferencesEditor.apply() }

    confirmVerified(sharedPreferencesEditor)
  }

//  @Test
//  fun `Should return true if count is one before last one and 10 days passed`() {
//    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 1
//    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns (nowUtcMillis() - TimeUnit.DAYS.toMillis(11))
//
//    val result = SUT.shouldShowRateApp()
//
//    assertThat(result).isTrue()
//    verify { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 2) }
//    verify { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
//    verify { sharedPreferencesEditor.apply() }
//
//    confirmVerified(sharedPreferencesEditor)
//  }
//
//  @Test
//  fun `Should return true if count is last one and 14 days passed`() {
//    every { sharedPreferences.getInt(KEY_RATE_APP_COUNT, any()) } returns 2
//    every { sharedPreferences.getLong(KEY_RATE_APP_TIMESTAMP, any()) } returns (nowUtcMillis() - TimeUnit.DAYS.toMillis(15))
//
//    val result = SUT.shouldShowRateApp()
//
//    assertThat(result).isTrue()
//    verify { sharedPreferencesEditor.putInt(KEY_RATE_APP_COUNT, 3) }
//    verify { sharedPreferencesEditor.putLong(KEY_RATE_APP_TIMESTAMP, any()) }
//    verify { sharedPreferencesEditor.apply() }
//    confirmVerified(sharedPreferencesEditor)
//  }
}
