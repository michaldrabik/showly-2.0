package com.michaldrabik.storage.database.dao.helpers

import com.michaldrabik.storage.database.model.Actor
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.storage.database.model.Settings
import com.michaldrabik.storage.database.model.Show

object TestData {

  fun createShow() = Show(
    1,
    1,
    1,
    "idImdb",
    "idSlug",
    1,
    "Title",
    2000,
    "Overview",
    "FirstAired",
    60,
    "AirtimeDay",
    "AirtimeTime",
    "AirtimeTimezone",
    "Certification",
    "Network",
    "Country",
    "Trailer",
    "Homepage",
    "Status",
    0F,
    0L,
    0L,
    "Genres",
    0,
    0
  )

  fun createSettings() = Settings(1, false, "", "", "")

  fun createActor() = Actor(0, 1, 1, "Name", "Role", 1, "Image", 99, 99)

  fun createEpisode() = Episode(1, 1, 1, 1, "", 1, 1, 1, "", "", null, 0, 0F, 60, 0, false)

  fun createSeason() = Season(1, 1, 1, "", "", null, 0, 0, false)
}