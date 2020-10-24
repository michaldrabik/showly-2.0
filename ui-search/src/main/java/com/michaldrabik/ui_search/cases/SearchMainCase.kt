package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import java.util.*
import javax.inject.Inject

@AppScope
class SearchMainCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  private val showsRepository: ShowsRepository
) {

  suspend fun searchShows(query: String): List<Show> {
    Analytics.logSearchQuery(query)
    val shows = cloud.traktApi.fetchShowsSearch(query)
    return shows.map { mappers.show.fromNetwork(it) }
  }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadSeeLaterShowsIds() = showsRepository.seeLaterShows.loadAllIds()

  suspend fun loadTranslation(show: Show): Translation? {
    val locale = Locale.getDefault()
    if (locale.language == Config.DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(show, locale, onlyLocal = true)
  }
}
