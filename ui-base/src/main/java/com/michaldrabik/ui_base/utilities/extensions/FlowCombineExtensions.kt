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
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R
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
  combineKtx(flow, flow2, flow3, ::Triple),
  combineKtx(flow4, flow5, flow6, ::Triple),
  combineKtx(flow7, flow8, flow9, ::Triple),
  combineKtx(flow10, flow11) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow12, flow13) { t1, t2 -> Pair(t1, t2) }
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
    t5.second,
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
    t4.third,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> combine(
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
  flow14: Flow<T14>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3, ::Triple),
  combineKtx(flow4, flow5, flow6, ::Triple),
  combineKtx(flow7, flow8, flow9, ::Triple),
  combineKtx(flow10, flow11, flow12, ::Triple),
  combineKtx(flow13, flow14, ::Pair)
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
    t4.third,
    t5.first,
    t5.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3, ::Triple),
  combineKtx(flow4, flow5, flow6, ::Triple),
  combineKtx(flow7, flow8, flow9, ::Triple),
  combineKtx(flow10, flow11, flow12, ::Triple),
  combineKtx(flow13, flow14, flow15, ::Triple)
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) -> R,
): Flow<R> = combineKtx(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15, flow16) { t1, t2, t3, t4 -> Pair(Pair(t1, t2), Pair(t3, t4)) }
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
    t4.third,
    t5.first.first,
    t5.first.second,
    t5.second.first,
    t5.second.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) -> R
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3, ::Triple),
  combineKtx(flow4, flow5, flow6, ::Triple),
  combineKtx(flow7, flow8, flow9, ::Triple),
  combineKtx(flow10, flow11, flow12, ::Triple),
  combineKtx(flow13, flow14, flow15, ::Triple),
  combineKtx(flow16, flow17, ::Pair)
) { t1, t2, t3, t4, t5, t6 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow18, flow19) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4, t5, t6, t7 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t7.first,
    t7.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  flow20: Flow<T20>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17, flow18) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow19, flow20) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4, t5, t6, t7 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t6.third,
    t7.first,
    t7.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  flow20: Flow<T20>,
  flow21: Flow<T21>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17, flow18) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow19, flow20, flow21) { t1, t2, t3 -> Triple(t1, t2, t3) }
) { t1, t2, t3, t4, t5, t6, t7 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t6.third,
    t7.first,
    t7.second,
    t7.third
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  flow20: Flow<T20>,
  flow21: Flow<T21>,
  flow22: Flow<T22>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17, flow18) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow19, flow20) { t1, t2 -> Pair(t1, t2) },
  combineKtx(flow21, flow22) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4, t5, t6, t7, t8 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t6.third,
    t7.first,
    t7.second,
    t8.first,
    t8.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  flow20: Flow<T20>,
  flow21: Flow<T21>,
  flow22: Flow<T22>,
  flow23: Flow<T23>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17, flow18) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow19, flow20, flow21) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow22, flow23) { t1, t2 -> Pair(t1, t2) }
) { t1, t2, t3, t4, t5, t6, t7, t8 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t6.third,
    t7.first,
    t7.second,
    t7.third,
    t8.first,
    t8.second
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24, R> combine(
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
  flow14: Flow<T14>,
  flow15: Flow<T15>,
  flow16: Flow<T16>,
  flow17: Flow<T17>,
  flow18: Flow<T18>,
  flow19: Flow<T19>,
  flow20: Flow<T20>,
  flow21: Flow<T21>,
  flow22: Flow<T22>,
  flow23: Flow<T23>,
  flow24: Flow<T24>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24) -> R,
): Flow<R> = combine(
  combineKtx(flow, flow2, flow3) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow4, flow5, flow6) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow7, flow8, flow9) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow10, flow11, flow12) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow13, flow14, flow15) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow16, flow17, flow18) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow19, flow20, flow21) { t1, t2, t3 -> Triple(t1, t2, t3) },
  combineKtx(flow22, flow23, flow24) { t1, t2, t3 -> Triple(t1, t2, t3) }
) { t1, t2, t3, t4, t5, t6, t7, t8 ->
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
    t4.third,
    t5.first,
    t5.second,
    t5.third,
    t6.first,
    t6.second,
    t6.third,
    t7.first,
    t7.second,
    t7.third,
    t8.first,
    t8.second,
    t8.third
  )
}
