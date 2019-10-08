package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Settings
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Settings as SettingsDb

class SettingsMapper @Inject constructor() {

  fun fromDatabase(settings: SettingsDb) = Settings(
    enumValueOf(settings.myShowsRunningSortBy),
    enumValueOf(settings.myShowsIncomingSortBy),
    enumValueOf(settings.myShowsEndedSortBy)
  )

  fun toDatabase(settings: Settings) = SettingsDb(
    myShowsRunningSortBy = settings.myShowsRunningSortBy.name,
    myShowsIncomingSortBy = settings.myShowsIncomingSortBy.name,
    myShowsEndedSortBy = settings.myShowsEndedSortBy.name
  )
}