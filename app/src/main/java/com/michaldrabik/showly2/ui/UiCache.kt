package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Genre
import com.michaldrabik.showly2.model.MyShowsSection
import javax.inject.Inject

@AppScope
class UiCache @Inject constructor() {

  var discoverChipsPosition = 0F
  var discoverSearchPosition = 0F
  var discoverActiveGenres = mutableListOf<Genre>()
  var myShowsListPosition = Pair(0, 0)
  var myShowsSectionPositions = mutableMapOf<MyShowsSection, Pair<Int, Int>>()

  fun clear() {
    discoverSearchPosition = 0F
    discoverChipsPosition = 0F
    discoverActiveGenres.clear()
    myShowsListPosition = Pair(0, 0)
    myShowsSectionPositions.clear()
  }
}