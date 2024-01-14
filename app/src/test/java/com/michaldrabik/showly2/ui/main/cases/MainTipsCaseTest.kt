package com.michaldrabik.showly2.ui.main.cases

import BaseMockTest
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.ui_model.Tip
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verifyAll
import org.junit.Before
import org.junit.Test

class MainTipsCaseTest : BaseMockTest() {

  @MockK
  lateinit var sharedPreferences: SharedPreferences

  @MockK
  lateinit var sharedPreferencesEditor: SharedPreferences.Editor

  private lateinit var SUT: MainTipsCase

  @Before
  override fun setUp() {
    super.setUp()
    every { sharedPreferencesEditor.apply() } just Runs
    every { sharedPreferencesEditor.putBoolean(any(), any()) } returns sharedPreferencesEditor
    every { sharedPreferences.edit() } returns sharedPreferencesEditor

    SUT = MainTipsCase(sharedPreferences)
  }

  @Test
  fun `Should return true if tip has been show`() {
    val tip = Tip.MENU_DISCOVER
    every { sharedPreferences.getBoolean(tip.name, false) } returns true

    assertThat(SUT.isTipShown(tip)).isTrue()
  }

  @Test
  fun `Should return false if tips has not been shown`() {
    val tip = Tip.MENU_DISCOVER
    every { sharedPreferences.getBoolean(tip.name, false) } returns false

    if (BuildConfig.DEBUG) {
      assertThat(SUT.isTipShown(tip)).isTrue()
    } else {
      assertThat(SUT.isTipShown(tip)).isFalse()
    }
  }

  @Test
  fun `Should store tips shown info properly`() {
    val tip = Tip.MENU_DISCOVER

    SUT.setTipShown(tip)

    verifyAll {
      sharedPreferencesEditor.putBoolean(tip.name, true)
      sharedPreferencesEditor.apply()
    }
  }
}
