package com.michaldrabik.common.dispatchers

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultCoroutineDispatchers @Inject constructor() : CoroutineDispatchers {
  override val Main = Dispatchers.Main
  override val IO = Dispatchers.IO
  override val Default = Dispatchers.Default
  override val Unconfined = Dispatchers.Unconfined
}
