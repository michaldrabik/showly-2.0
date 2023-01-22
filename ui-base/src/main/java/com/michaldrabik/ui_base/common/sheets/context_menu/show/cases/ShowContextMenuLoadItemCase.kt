package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.repository.OnHoldItemsRepository
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuLoadItemCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadItem(traktId: IdTrakt) = withContext(Dispatchers.IO) {
    val show = showsRepository.detailsShow.load(traktId)

    val imageAsync = async { imagesProvider.findCachedImage(show, ImageType.POSTER) }
    val translationAsync = async { translationsRepository.loadTranslation(show, language = settingsRepository.language, onlyLocal = true) }
    val ratingAsync = async { ratingsRepository.shows.loadRatings(listOf(show)) }

    val isMyShowAsync = async { showsRepository.myShows.exists(traktId) }
    val isWatchlistAsync = async { showsRepository.watchlistShows.exists(traktId) }
    val isHiddenAsync = async { showsRepository.hiddenShows.exists(traktId) }

    val isPinnedAsync = async { pinnedItemsRepository.isItemPinned(show) }
    val isOnHoldAsync = async { onHoldItemsRepository.isOnHold(show) }

    ShowContextItem(
      show = show,
      image = imageAsync.await(),
      translation = translationAsync.await(),
      userRating = ratingAsync.await().firstOrNull()?.rating,
      isMyShow = isMyShowAsync.await(),
      isWatchlist = isWatchlistAsync.await(),
      isHidden = isHiddenAsync.await(),
      isPinnedTop = isPinnedAsync.await(),
      isOnHold = isOnHoldAsync.await()
    )
  }
}
