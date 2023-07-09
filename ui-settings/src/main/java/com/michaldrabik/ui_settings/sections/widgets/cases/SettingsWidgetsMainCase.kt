package com.michaldrabik.ui_settings.sections.widgets.cases

import android.content.Context
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_model.Settings
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsWidgetsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun getSettings(): Settings = withContext(dispatchers.IO) {
    settingsRepository.load()
  }

  fun isPremium() = settingsRepository.isPremium

  suspend fun enableWidgetsTitles(enable: Boolean, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(widgetsShowLabel = enable)
      settingsRepository.update(new)
    }
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }
}
