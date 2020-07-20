package com.michaldrabik.showly2.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

  companion object {
    private const val ARG_DISCOVER_SEARCH_POS = "ARG_DISCOVER_SEARCH_POS"
  }

  private var isRestored = false

  var discoverSearchViewPosition = 0F

  @Suppress("UNCHECKED_CAST")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!isRestored) {
      isRestored = true
      savedInstanceState?.let {
        discoverSearchViewPosition = it.getFloat(ARG_DISCOVER_SEARCH_POS, 0F)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat(ARG_DISCOVER_SEARCH_POS, discoverSearchViewPosition)
  }

  protected fun clearUiCache() {
    discoverSearchViewPosition = 0F
  }
}
