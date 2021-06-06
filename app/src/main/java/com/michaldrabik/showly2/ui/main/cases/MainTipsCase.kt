package com.michaldrabik.showly2.ui.main.cases

import android.content.SharedPreferences
import com.michaldrabik.common.Config
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.ui_model.Tip
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import javax.inject.Named

@ViewModelScoped
class MainTipsCase @Inject constructor(
  @Named("tipsPreferences") private val sharedPreferences: SharedPreferences
) {

  fun isTipShown(tip: Tip) = when {
    BuildConfig.DEBUG -> !Config.SHOW_TIPS_DEBUG
    else -> sharedPreferences.getBoolean(tip.name, false)
  }

  fun setTipShown(tip: Tip) {
    sharedPreferences.edit().putBoolean(tip.name, true).apply()
  }
}
