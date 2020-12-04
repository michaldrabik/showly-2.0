package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SearchMainCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository
) {

  suspend fun searchByQuery(query: String): List<SearchResult> {
    Analytics.logSearchQuery(query)
    val results = cloud.traktApi.fetchSearch(query)
      .sortedWith(compareBy({ it.score }, { it.votes ?: 0 }))
      .reversed()
    return results.map {
      SearchResult(
        it.score ?: 0F,
        it.show?.let { s -> mappers.show.fromNetwork(s) } ?: Show.EMPTY,
        it.movie?.let { m -> mappers.movie.fromNetwork(m) } ?: Movie.EMPTY,
      )
    }
  }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadWatchlistShowsIds() = showsRepository.watchlistShows.loadAllIds()

  suspend fun loadTranslation(searchResult: SearchResult): Translation? {
    val language = settingsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return null
    return when {
      searchResult.isShow -> translationsRepository.loadTranslation(searchResult.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(searchResult.movie, language, onlyLocal = true)
    }
  }
}
