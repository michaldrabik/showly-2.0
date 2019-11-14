package com.michaldrabik.showly2.ui.settings

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class SettingsInteractor @Inject constructor(
  private val database: AppDatabase
) {

}