package com.michaldrabik.ui_model

import android.content.Context
import androidx.annotation.StringRes

enum class PremiumFeature(@StringRes val tag: Int) {
  THEME(R.string.tagTheme),
  NEWS(R.string.tagNews),
  WIDGET_TRANSPARENCY(R.string.tagWidgetTransparency),
  QUICK_RATING(R.string.tagQuickRating),
  CUSTOM_IMAGES(R.string.tagCustomImages),
  VIEW_TYPES(R.string.tagViewsTypes);

  companion object {
    fun fromTag(
      context: Context,
      tag: String
    ) = values().firstOrNull { context.getString(it.tag) == tag }
  }
}
