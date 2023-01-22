package com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuLoadItemCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadItem(traktId: IdTrakt) = withContext(Dispatchers.IO) {
    val movie = moviesRepository.movieDetails.load(traktId)
    val dateFormat = dateFormatProvider.loadShortDayFormat()
    val language = translationsRepository.getLanguage()

    val imageAsync = async { imagesProvider.findCachedImage(movie, ImageType.POSTER) }
    val translationAsync = async { translationsRepository.loadTranslation(movie, language = language, onlyLocal = true) }
    val ratingAsync = async { ratingsRepository.movies.loadRatings(listOf(movie)) }

    val isMyMovieAsync = async { moviesRepository.myMovies.exists(traktId) }
    val isWatchlistAsync = async { moviesRepository.watchlistMovies.exists(traktId) }
    val isHiddenAsync = async { moviesRepository.hiddenMovies.exists(traktId) }

    val isPinnedAsync = async { pinnedItemsRepository.isItemPinned(movie) }

    MovieContextItem(
      movie = movie,
      image = imageAsync.await(),
      translation = translationAsync.await(),
      userRating = ratingAsync.await().firstOrNull()?.rating,
      isMyMovie = isMyMovieAsync.await(),
      isWatchlist = isWatchlistAsync.await(),
      isHidden = isHiddenAsync.await(),
      isPinnedTop = isPinnedAsync.await(),
      dateFormat = dateFormat
    )
  }
}
