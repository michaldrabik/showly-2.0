package com.michaldrabik.common_test

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedCoroutineDispatchers : CoroutineDispatchers {
  override val Main = UnconfinedTestDispatcher()
  override val IO = UnconfinedTestDispatcher()
  override val Default = UnconfinedTestDispatcher()
  override val Unconfined = UnconfinedTestDispatcher()
}
