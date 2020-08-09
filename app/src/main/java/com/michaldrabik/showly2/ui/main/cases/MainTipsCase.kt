package com.michaldrabik.showly2.ui.main.cases

import android.content.SharedPreferences
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Tip
import javax.inject.Inject
import javax.inject.Named

@AppScope
class MainTipsCase @Inject constructor(
  @Named("tipsPreferences") private val sharedPreferences: SharedPreferences
) {

  fun isTipShown(tip: Tip) = when {
    BuildConfig.DEBUG -> true
    else -> sharedPreferences.getBoolean(tip.name, false)
  }

  fun setTipShown(tip: Tip) {
    sharedPreferences.edit().putBoolean(tip.name, true).apply()
  }
}
