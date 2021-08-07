package com.michaldrabik.ui_base.utilities.extensions

import kotlinx.coroutines.flow.Flow

fun <T1, T2, T3, T4, T5, T6, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
  kotlinx.coroutines.flow.combine(flow, flow2, flow3, ::Triple),
  kotlinx.coroutines.flow.combine(flow4, flow5, flow6, ::Triple)
) { t1, t2 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
  kotlinx.coroutines.flow.combine(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  kotlinx.coroutines.flow.combine(flow4, flow5) { t1, t2 -> Pair(t1, t2) },
  kotlinx.coroutines.flow.combine(flow6, flow7) { t1, t2 -> Pair(t1, t2) },
) { t1, t2, t3 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t3.first,
    t3.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
  kotlinx.coroutines.flow.combine(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  kotlinx.coroutines.flow.combine(flow4, flow5) { t1, t2 -> Pair(t1, t2) },
  kotlinx.coroutines.flow.combine(flow6, flow7, flow8) { t1, t2, t3 -> Triple(t1, t2, t3) },
) { t1, t2, t3 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t3.first,
    t3.second,
    t3.third
  )
}
