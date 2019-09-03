package com.michaldrabik.showly2.ui

import com.michaldrabik.showly2.di.AppScope
import javax.inject.Inject

@AppScope
class UiCache @Inject constructor() {

  var discoverListPosition: Pair<Int, Int> = Pair(0, 0)
}