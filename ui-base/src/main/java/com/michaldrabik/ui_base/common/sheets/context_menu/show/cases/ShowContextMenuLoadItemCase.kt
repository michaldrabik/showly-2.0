package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuLoadItemCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun loadItem(traktId: IdTrakt) = coroutineScope {
    val showDetails = showsRepository.detailsShow.load(traktId)

    val imageAsync = async { imagesProvider.findCachedImage(showDetails, ImageType.POSTER) }
    val translationAsync = async { translationsRepository.loadTranslation(showDetails, onlyLocal = true) }

    val isMyShowAsync = async { showsRepository.myShows.exists(traktId) }
    val isWatchlistAsync = async { showsRepository.watchlistShows.exists(traktId) }
    val isHiddenAsync = async { showsRepository.hiddenShows.exists(traktId) }

    ShowContextItem(
      show = showDetails,
      image = imageAsync.await(),
      translation = translationAsync.await(),
      isMyShow = isMyShowAsync.await(),
      isWatchlist = isWatchlistAsync.await(),
      isHidden = isHiddenAsync.await()
    )
  }
}
