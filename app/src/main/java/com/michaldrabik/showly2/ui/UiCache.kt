package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.MyShowsSection
import javax.inject.Inject

@AppScope
class UiCache @Inject constructor() {

  var discoverSearchPosition = 0F
  var myShowsSectionPositions = mutableMapOf<MyShowsSection, Pair<Int, Int>>()

  fun clear() {
    discoverSearchPosition = 0F
    myShowsSectionPositions.clear()
  }
}
