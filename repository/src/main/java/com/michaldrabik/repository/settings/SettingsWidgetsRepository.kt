package com.michaldrabik.repository.settings

import android.app.UiModeManager
import android.content.SharedPreferences
import androidx.core.content.edit
import com.michaldrabik.common.CalendarMode
import com.michaldrabik.common.delegates.BooleanPreference
import com.michaldrabik.common.delegates.EnumPreference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsWidgetsRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences
) {

  companion object Key {
    private const val THEME_WIDGET = "KEY_THEME_WIDGET"
    private const val THEME_WIDGET_TRANSPARENT = "KEY_THEME_WIDGET_TRANSPARENT"
    private const val WIDGET_CALENDAR_MODE = "WIDGET_CALENDAR_MODE"
    private const val WIDGET_CALENDAR_MOVIES_MODE = "WIDGET_CALENDAR_MOVIES_MODE"
  }

  val isPremium by BooleanPreference(preferences, SettingsRepository.PREMIUM)

  var widgetCalendarMoviesMode by EnumPreference(preferences, WIDGET_CALENDAR_MOVIES_MODE, CalendarMode.PRESENT_FUTURE, CalendarMode::class.java)

  var widgetsTheme: Int
    get() {
      if (!isPremium) return UiModeManager.MODE_NIGHT_YES
      return preferences.getInt(THEME_WIDGET, UiModeManager.MODE_NIGHT_YES)
    }
    set(value) = preferences.edit(true) { putInt(THEME_WIDGET, value) }

  var widgetsTransparency: Int
    get() {
      if (!isPremium) return 100
      return preferences.getInt(THEME_WIDGET_TRANSPARENT, 100)
    }
    set(value) = preferences.edit(true) { putInt(THEME_WIDGET_TRANSPARENT, value) }

  fun getWidgetCalendarMode(widgetId: Int): CalendarMode {
    val default = CalendarMode.PRESENT_FUTURE.name
    val value = preferences.getString(WIDGET_CALENDAR_MODE + widgetId, default) ?: default
    return CalendarMode.valueOf(value)
  }

  fun setWidgetCalendarMode(widgetId: Int, mode: CalendarMode) {
    preferences.edit(true) { putString(WIDGET_CALENDAR_MODE + widgetId, mode.name) }
  }

  fun revokePremium() {
    widgetsTheme = UiModeManager.MODE_NIGHT_YES
    widgetsTransparency = 100
  }
}
