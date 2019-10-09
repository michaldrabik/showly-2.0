package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.MyShowsSection
import javax.inject.Inject

@AppScope
class UiCache @Inject constructor() {

  var discoverListPosition = Pair(0, 0)
  var myShowsListPosition = Pair(0, 0)
  var myShowsSectionPositions = mutableMapOf<MyShowsSection, Pair<Int, Int>>()

  fun clear() {
    discoverListPosition = Pair(0, 0)
    myShowsListPosition = Pair(0, 0)
    myShowsSectionPositions.clear()
  }
}