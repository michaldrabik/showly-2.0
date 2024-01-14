package com.michaldrabik.data_local.database.dao.helpers

import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.database.model.Settings
import com.michaldrabik.data_local.database.model.Show

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
    0,
    0
  )

  fun createSettings() = Settings(
    1,
    false,
    true,
    true,
    0,
    2,
    "",
    "",
    "",
    "",
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    "",
    true,
    "OFF",
    "",
    "",
    false,
    false,
    "",
    "",
    false,
    false,
    false,
    "",
    "",
    "",
    "",
    "",
    true,
    true,
    true,
    true,
    true,
    "",
    true
  )

  fun createEpisode() = Episode(1, 1, 1, 1, "", 1, 1, 1, 1, "", "", null, 0, 0F, 60, 0, false)

  fun createSeason() = Season(1, 1, 1, "", "", null, 0, 0, 0f, false)
}
