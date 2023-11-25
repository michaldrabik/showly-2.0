package com.michaldrabik.ui_base.utilities.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.combine as combineKtx

fun <T1, R> combine(
  flow: Flow<T1>,
  transform: suspend (T1) -> R,
): Flow<R> = combineKtx(
  flow,
  emptyFlow<Any>()
) { t1, _ ->
  transform(t1)
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3, ::Triple),
  combineKtx(flow4, flow5, flow6, ::Triple)
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
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow6, flow7) { t1, t2 -> Pair(t1, t2) },
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
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow6, flow7, flow8) { t1, t2, t3 -> Triple(t1, t2, t3) },
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

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) }
) { t1, t2, t3 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third,
    t3.first,
    t3.second,
    t3.third
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  flow10: Flow<T10>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow9, flow10) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third,
    t3.first,
    t3.second,
    t4.first,
    t4.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  flow10: Flow<T10>,
  flow11: Flow<T11>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third,
    t3.first,
    t3.second,
    t3.third,
    t4.first,
    t4.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  flow10: Flow<T10>,
  flow11: Flow<T11>,
  flow12: Flow<T12>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) }
) { t1, t2, t3, t4 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third,
    t3.first,
    t3.second,
    t3.third,
    t4.first,
    t4.second,
    t4.third
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  flow10: Flow<T10>,
  flow11: Flow<T11>,
  flow12: Flow<T12>,
  flow13: Flow<T13>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow12, flow13) { t1, t2 -> Pair(t1, t2) },
) { t1, t2, t3, t4, t5 ->
  transform(
    t1.first,
    t1.second,
    t1.third,
    t2.first,
    t2.second,
    t2.third,
    t3.first,
    t3.second,
    t3.third,
    t4.first,
    t4.second,
    t5.first,
    t5.second
  )
}
