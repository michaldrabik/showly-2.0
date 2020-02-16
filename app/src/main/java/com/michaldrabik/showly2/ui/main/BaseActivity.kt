package com.michaldrabik.showly2.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

  companion object {
    private const val ARG_DISCOVER_SEARCH_POS = "ARG_DISCOVER_SEARCH_POS"
    private const val ARG_MY_SHOWS_RUNNING_POS = "ARG_DISCOVER_SEARCH_POS"
    private const val ARG_MY_SHOWS_ENDED_POS = "MY_SHOWS_ENDED_POS"
    private const val ARG_MY_SHOWS_INCOMING_POS = "MY_SHOWS_INCOMING_POS"
  }

  private var isRestored = false

  var discoverSearchViewPosition = 0F
  var myShowsRunningPosition = Pair(0, 0)
  var myShowsEndedPosition = Pair(0, 0)
  var myShowsIncomingPosition = Pair(0, 0)

  @Suppress("UNCHECKED_CAST")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!isRestored) {
      isRestored = true
      savedInstanceState?.let {
        discoverSearchViewPosition = it.getFloat(ARG_DISCOVER_SEARCH_POS, 0F)
        myShowsRunningPosition = it.getSerializable(ARG_MY_SHOWS_RUNNING_POS) as Pair<Int, Int>
        myShowsEndedPosition = it.getSerializable(ARG_MY_SHOWS_ENDED_POS) as Pair<Int, Int>
        myShowsIncomingPosition = it.getSerializable(ARG_MY_SHOWS_INCOMING_POS) as Pair<Int, Int>
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat(ARG_DISCOVER_SEARCH_POS, discoverSearchViewPosition)
    outState.putSerializable(ARG_MY_SHOWS_RUNNING_POS, myShowsRunningPosition)
    outState.putSerializable(ARG_MY_SHOWS_ENDED_POS, myShowsEndedPosition)
    outState.putSerializable(ARG_MY_SHOWS_INCOMING_POS, myShowsIncomingPosition)
  }

  protected fun clearUiCache() {
    discoverSearchViewPosition = 0F
    myShowsRunningPosition = Pair(0, 0)
    myShowsEndedPosition = Pair(0, 0)
    myShowsIncomingPosition = Pair(0, 0)
  }
}
