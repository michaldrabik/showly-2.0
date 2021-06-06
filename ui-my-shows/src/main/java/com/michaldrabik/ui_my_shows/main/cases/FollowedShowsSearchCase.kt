package com.michaldrabik.ui_my_shows.main.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class FollowedShowsSearchCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider
) {

  private val searchCache = mutableListOf<Show>()
  private val searchTranslationsCache = mutableMapOf<Long, Translation>()

  suspend fun searchFollowed(query: String?): List<MyShowsItem> {
    if (query.isNullOrBlank()) return emptyList()

    if (searchCache.isEmpty()) {
      val collection = showsRepository.loadCollection()
      searchCache.clear()
      searchCache.addAll(collection)
    }

    val language = translationsRepository.getLanguage()
    if (searchTranslationsCache.isEmpty() && language != DEFAULT_LANGUAGE) {
      val collection = translationsRepository.loadAllShowsLocal(language)
      searchTranslationsCache.clear()
      searchTranslationsCache.putAll(collection)
    }

    return searchCache
      .filter {
        it.title.contains(query, true) ||
          it.network.contains(query, true) ||
          searchTranslationsCache[it.traktId]?.title?.contains(query, true) == true
      }
      .sortedBy { it.title }
      .take(30)
      .map {
        val image = imagesProvider.findCachedImage(it, ImageType.FANART)
        MyShowsItem.createSearchItem(it, image, searchTranslationsCache[it.traktId])
      }
  }

  fun clearCache() {
    searchCache.clear()
    searchTranslationsCache.clear()
  }
}
