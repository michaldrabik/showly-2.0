package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Settings
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Settings as SettingsDb

class SettingsMapper @Inject constructor() {

  fun fromDatabase(settings: SettingsDb) = Settings(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    showsNotificationsEnabled = settings.showsNotificationsEnabled,
    myShowsRunningSortBy = enumValueOf(settings.myShowsRunningSortBy),
    myShowsIncomingSortBy = enumValueOf(settings.myShowsIncomingSortBy),
    myShowsEndedSortBy = enumValueOf(settings.myShowsEndedSortBy),
    myShowsRecentsAmount = settings.myShowsRecentsAmount
  )

  fun toDatabase(settings: Settings) = SettingsDb(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    showsNotificationsEnabled = settings.showsNotificationsEnabled,
    myShowsRunningSortBy = settings.myShowsRunningSortBy.name,
    myShowsIncomingSortBy = settings.myShowsIncomingSortBy.name,
    myShowsEndedSortBy = settings.myShowsEndedSortBy.name,
    myShowsRecentsAmount = settings.myShowsRecentsAmount
  )
}