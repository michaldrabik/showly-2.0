package com.michaldrabik.ui_discover.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_discover.helpers.itemtype.ImageTypeProvider
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
internal class DiscoverShowsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val imageTypeProvider: ImageTypeProvider,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun isCacheValid() =
    withContext(dispatchers.IO) {
      showsRepository.discoverShows.isCacheValid()
    }

  suspend fun loadCachedShows(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val myShowsIds = async { showsRepository.myShows.loadAllIds() }
      val watchlistShowsIds = async { showsRepository.watchlistShows.loadAllIds() }
      val archiveShowsIds = async { showsRepository.hiddenShows.loadAllIds() }
      val cachedShows = async { showsRepository.discoverShows.loadAllCached() }

      prepareItems(
        shows = cachedShows.await(),
        myShowsIds = myShowsIds.await(),
        watchlistShowsIds = watchlistShowsIds.await(),
        hiddenShowsIds = archiveShowsIds.await(),
        filters = filters,
      )
    }

  suspend fun loadRemoteShows(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val showAnticipated = !filters.hideAnticipated
      val showCollection = !filters.hideCollection
      val genres = filters.genres.toList()
      val networks = filters.networks.toList()

      val myAsync = async { showsRepository.myShows.loadAllIds() }
      val watchlistSync = async { showsRepository.watchlistShows.loadAllIds() }
      val archiveAsync = async { showsRepository.hiddenShows.loadAllIds() }
      val (myIds, watchlistIds, hiddenIds) = awaitAll(myAsync, watchlistSync, archiveAsync)
      val collectionSize = myIds.size + watchlistIds.size + hiddenIds.size

      val remoteShows = showsRepository.discoverShows.loadAllRemote(
        showAnticipated,
        showCollection,
        collectionSize,
        genres,
        networks,
      )

      showsRepository.discoverShows.cacheDiscoverShows(remoteShows)

      prepareItems(
        shows = remoteShows,
        myShowsIds = myIds,
        watchlistShowsIds = watchlistIds,
        hiddenShowsIds = hiddenIds,
        filters = filters,
      )
    }

  private suspend fun prepareItems(
    shows: List<Show>,
    myShowsIds: List<Long>,
    watchlistShowsIds: List<Long>,
    hiddenShowsIds: List<Long>,
    filters: DiscoverFilters?,
  ) = coroutineScope {
    val language = translationsRepository.getLanguage()
    val collectionIds = myShowsIds + watchlistShowsIds + hiddenShowsIds
    shows
      .filter { it.traktId !in hiddenShowsIds }
      .filter {
        if (filters?.hideCollection == false) {
          true
        } else {
          it.traktId !in collectionIds
        }
      }
      .sortedBy(filters?.feedOrder ?: HOT)
      .mapIndexed { index, show ->
        async {
          val itemType = imageTypeProvider.getImageType(index)
          val image = imagesProvider.findCachedImage(show, itemType)
          val translation = loadTranslation(language, itemType, show)
          DiscoverListItem(
            show = show,
            image = image,
            isFollowed = show.traktId in myShowsIds,
            isWatchlist = show.traktId in watchlistShowsIds,
            translation = translation,
          )
        }
      }.awaitAll()
      .toMutableList()
      .apply { insertTwitterAdItem(this) }
      .toList()
  }

  private fun insertTwitterAdItem(items: MutableList<DiscoverListItem>) {
    val isEnabled = settingsRepository.isTwitterAdEnabled
    val isTimePassed = (nowUtcMillis() - settingsRepository.installTimestamp) > ConfigVariant.TWITTER_AD_DELAY
    if (!isEnabled || !isTimePassed) return

    val twitterAd = DiscoverListItem(Show.EMPTY, Image.createUnknown(ImageType.TWITTER))
    if (items.size >= imageTypeProvider.twitterAdPosition) {
      items.add(imageTypeProvider.twitterAdPosition, twitterAd)
    } else {
      items.add(twitterAd)
    }
  }

  private suspend fun loadTranslation(
    language: String,
    itemType: ImageType,
    show: Show,
  ) = if (language == Config.DEFAULT_LANGUAGE || itemType == ImageType.POSTER) {
    null
  } else {
    translationsRepository.loadTranslation(show, language, true)
  }

  private fun List<Show>.sortedBy(order: DiscoverSortOrder) =
    when (order) {
      HOT -> this
      RATING -> this.sortedWith(compareByDescending<Show> { it.votes }.thenBy { it.rating })
      NEWEST -> this.sortedByDescending { it.year }
    }
}
