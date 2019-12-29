package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.storage.database.model.Settings as SettingsDb
import javax.inject.Inject

class SettingsMapper @Inject constructor() {

  fun fromDatabase(settings: SettingsDb) = Settings(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    episodesNotificationsEnabled = settings.episodesNotificationsEnabled,
    episodesNotificationsDelay = NotificationDelay.fromDelay(settings.episodesNotificationsDelay),
    myShowsRunningSortBy = enumValueOf(settings.myShowsRunningSortBy),
    myShowsIncomingSortBy = enumValueOf(settings.myShowsIncomingSortBy),
    myShowsEndedSortBy = enumValueOf(settings.myShowsEndedSortBy),
    myShowsAllSortBy = enumValueOf(settings.myShowsAllSortBy),
    myShowsRecentsAmount = settings.myShowsRecentsAmount,
    seeLaterShowsSortBy = enumValueOf(settings.seeLaterShowsSortBy)
  )

  fun toDatabase(settings: Settings) = SettingsDb(
    isInitialRun = settings.isInitialRun,
    pushNotificationsEnabled = settings.pushNotificationsEnabled,
    episodesNotificationsEnabled = settings.episodesNotificationsEnabled,
    episodesNotificationsDelay = settings.episodesNotificationsDelay.delayMs,
    myShowsRunningSortBy = settings.myShowsRunningSortBy.name,
    myShowsIncomingSortBy = settings.myShowsIncomingSortBy.name,
    myShowsEndedSortBy = settings.myShowsEndedSortBy.name,
    myShowsAllSortBy = settings.myShowsAllSortBy.name,
    myShowsRecentsAmount = settings.myShowsRecentsAmount,
    seeLaterShowsSortBy = settings.seeLaterShowsSortBy.name
  )
}
