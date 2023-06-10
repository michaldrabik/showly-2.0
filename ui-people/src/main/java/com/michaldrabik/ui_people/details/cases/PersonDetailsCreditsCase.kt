package com.michaldrabik.ui_people.details.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.PersonCredit
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsCreditsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val peopleRepository: PeopleRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  suspend fun loadCredits(person: Person, filters: List<Mode>) =
    withContext(dispatchers.IO) {
      val myShowsIdsAsync = async { showsRepository.myShows.loadAllIds() }
      val myMoviesIdsAsync = async { moviesRepository.myMovies.loadAllIds() }
      val watchlistShowsIdsAsync = async { showsRepository.watchlistShows.loadAllIds() }
      val watchlistMoviesIdsAsync = async { moviesRepository.watchlistMovies.loadAllIds() }
      val spoilers = settingsRepository.spoilers.getAll()

      val (myShowsIds, myMoviesIds, watchlistShowsId, watchlistMoviesIds) = awaitAll(
        myShowsIdsAsync,
        myMoviesIdsAsync,
        watchlistShowsIdsAsync,
        watchlistMoviesIdsAsync
      )

      val credits = peopleRepository.loadCredits(person)
      credits
        .filter {
          when {
            filters.isEmpty() || filters.containsAll(Mode.values().toList()) -> true
            filters.contains(Mode.SHOWS) -> it.show != null
            filters.contains(Mode.MOVIES) -> it.movie != null
            else -> true
          }
        }
        .filter { it.releaseDate != null || (it.releaseDate == null && it.isUpcoming) }
        .sortedWith(
          compareByDescending<PersonCredit> { it.releaseDate == null }.thenByDescending { it.releaseDate?.toEpochDay() }
        )
        .map {
          async {
            when {
              it.show != null -> createShowItem(
                show = it.requireShow(),
                myShowsIds = myShowsIds,
                watchlistShowsId = watchlistShowsId,
                spoilersSettings = spoilers
              )
              it.movie != null -> createMovieItem(
                movie = it.requireMovie(),
                myMoviesIds = myMoviesIds,
                watchlistMoviesId = watchlistMoviesIds,
                spoilersSettings = spoilers
              )
              else -> throw IllegalStateException()
            }
          }
        }
        .awaitAll()
        .groupBy { it.getReleaseDate()?.year }
    }

  private suspend fun createShowItem(
    show: Show,
    myShowsIds: List<Long>,
    watchlistShowsId: List<Long>,
    spoilersSettings: SpoilersSettings
  ) = show.let {
    val isMy = it.traktId in myShowsIds
    val isWatchlist = it.traktId in watchlistShowsId
    val image = showImagesProvider.findCachedImage(it, ImageType.POSTER)
    val translation = when (val language = translationsRepository.getLanguage()) {
      Config.DEFAULT_LANGUAGE -> null
      else -> translationsRepository.loadTranslation(it, language, onlyLocal = true)
    }
    PersonDetailsItem.CreditsShowItem(
      show = it,
      image = image,
      isMy = isMy,
      isWatchlist = isWatchlist,
      translation = translation,
      spoilers = spoilersSettings
    )
  }

  private suspend fun createMovieItem(
    movie: Movie,
    myMoviesIds: List<Long>,
    watchlistMoviesId: List<Long>,
    spoilersSettings: SpoilersSettings
  ) = movie.let {
    val isMy = it.traktId in myMoviesIds
    val isWatchlist = it.traktId in watchlistMoviesId
    val image = movieImagesProvider.findCachedImage(it, ImageType.POSTER)
    val translation = when (val language = translationsRepository.getLanguage()) {
      Config.DEFAULT_LANGUAGE -> null
      else -> translationsRepository.loadTranslation(it, language, onlyLocal = true)
    }
    PersonDetailsItem.CreditsMovieItem(
      movie = it,
      image = image,
      isMy = isMy,
      isWatchlist = isWatchlist,
      translation = translation,
      spoilers = spoilersSettings,
      moviesEnabled = settingsRepository.isMoviesEnabled
    )
  }
}
