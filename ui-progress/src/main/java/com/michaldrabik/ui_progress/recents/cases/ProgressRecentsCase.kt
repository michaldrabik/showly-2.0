package com.michaldrabik.ui_progress.recents.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import com.michaldrabik.ui_progress.recents.recycler.RecentsListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

@ViewModelScoped
class ProgressRecentsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  @Suppress("UNCHECKED_CAST")
  suspend fun loadRecentItems(): List<RecentsListItem> = coroutineScope {
    val now = nowUtc().toLocalZone()
    val language = translationsRepository.getLanguage()

    val shows = showsRepository.myShows.loadAll()
    val showsIds = shows.map { it.traktId }

    val (episodes, seasons) = awaitAll(
      async { database.episodesDao().getAllByShowsIds(showsIds) },
      async { database.seasonsDao().getAllByShowsIds(showsIds) }
    )

    val filteredSeasons = (seasons as List<Season>)
    val filteredEpisodes = (episodes as List<Episode>)
      .filter {
        val dateDays = it.firstAired?.toLocalZone()?.truncatedTo(DAYS)
        val isHistory = dateDays?.isBefore(now.truncatedTo(DAYS)) == true
        val isLast3Months = dateDays?.isAfter(now.truncatedTo(DAYS).minusMonths(3)) == true
        it.seasonNumber != 0 && isHistory && isLast3Months
      }

    val elements = filteredEpisodes
      .sortedWith(compareByDescending<Episode> { it.firstAired }.thenByDescending { it.episodeNumber })
      .map { episode ->
        async {
          val show = shows.first { it.traktId == episode.idShowTrakt }
          val season = filteredSeasons.first { it.idShowTrakt == episode.idShowTrakt && it.seasonNumber == episode.seasonNumber }
          val seasonEpisodes = episodes.filter { it.idShowTrakt == season.idShowTrakt && it.seasonNumber == season.seasonNumber }

          val episodeUi = mappers.episode.fromDatabase(episode)
          val seasonUi = mappers.season.fromDatabase(season, seasonEpisodes)

          var translations: TranslationsBundle? = null
          if (language != Config.DEFAULT_LANGUAGE) {
            translations = TranslationsBundle(
              episode = translationsRepository.loadTranslation(episodeUi, show.ids.trakt, language, onlyLocal = true),
              show = translationsRepository.loadTranslation(show, language, onlyLocal = true)
            )
          }
          RecentsListItem.Episode(
            show = show,
            image = imagesProvider.findCachedImage(show, ImageType.POSTER),
            episode = episodeUi,
            season = seasonUi,
            isWatched = episode.isWatched,
            dateFormat = dateFormatProvider.loadFullHourFormat(),
            translations = translations
          )
        }
      }.awaitAll()

    groupByTime(elements)
  }

  private fun groupByTime(items: List<RecentsListItem.Episode>): List<RecentsListItem> {
    val now = nowUtc().toLocalZone().truncatedTo(DAYS)

    val yesterdayItems = items.filter {
      val dateDays = it.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
      dateDays?.isEqual(now.minusDays(1)) == true
    }
    val last7DaysItems = (items - yesterdayItems).filter {
      val dateDays = it.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
      dateDays?.isAfter(now.minusDays(8)) == true
    }
    val last30DaysItems = (items - yesterdayItems - last7DaysItems).filter {
      val dateDays = it.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
      dateDays?.isAfter(now.minusDays(31)) == true
    }
    val last90Days = (items - yesterdayItems - last7DaysItems - last30DaysItems).filter {
      val dateDays = it.episode.firstAired?.toLocalZone()?.truncatedTo(DAYS)
      dateDays?.isAfter(now.minusDays(91)) == true
    }

    val itemsMap = mutableMapOf<Int, List<RecentsListItem>>()
      .apply {
        put(R.string.textYesterday, yesterdayItems)
        put(R.string.textLast7Days, last7DaysItems)
        put(R.string.textLast30Days, last30DaysItems)
        put(R.string.textLast90Days, last90Days)
      }

    return itemsMap.entries.fold(mutableListOf(), { acc, entry ->
      acc.apply {
        if (entry.value.isNotEmpty()) {
          add(RecentsListItem.Header.create(entry.key))
          addAll(entry.value)
        }
      }
    })
  }
}
