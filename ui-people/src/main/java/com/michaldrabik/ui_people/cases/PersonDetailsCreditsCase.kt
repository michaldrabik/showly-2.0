package com.michaldrabik.ui_people.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.PersonCredit
import com.michaldrabik.ui_people.recycler.PersonDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsCreditsCase @Inject constructor(
  private val peopleRepository: PeopleRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  private val moviesEnabled by lazy { settingsRepository.isMoviesEnabled }

  suspend fun loadCredits(
    person: Person,
    filters: List<Mode>,
    language: String
  ): Map<Int?, List<PersonDetailsItem>> = coroutineScope {

    val myShowsIdsAsync = async { showsRepository.myShows.loadAllIds() }
    val myMoviesIdsAsync = async { moviesRepository.myMovies.loadAllIds() }
    val watchlistShowsIdsAsync = async { showsRepository.watchlistShows.loadAllIds() }
    val watchlistMoviesIdsAsync = async { moviesRepository.watchlistMovies.loadAllIds() }

    val myShowsIds = myShowsIdsAsync.await()
    val myMoviesIds = myMoviesIdsAsync.await()
    val watchlistShowsId = watchlistShowsIdsAsync.await()
    val watchlistMoviesIds = watchlistMoviesIdsAsync.await()

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
        it.show?.let { show ->
          val isMy = show.traktId in myShowsIds
          val isWatchlist = show.traktId in watchlistShowsId
          val image = showImagesProvider.findCachedImage(show, ImageType.POSTER)
          val translation = when (language) {
            Config.DEFAULT_LANGUAGE -> null
            else -> translationsRepository.loadTranslation(show, language, onlyLocal = true)
          }
          return@map PersonDetailsItem.CreditsShowItem(show, image, isMy, isWatchlist, translation)
        }
        it.movie?.let { movie ->
          val isMy = movie.traktId in myMoviesIds
          val isWatchlist = movie.traktId in watchlistMoviesIds
          val image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)
          val translation = when (language) {
            Config.DEFAULT_LANGUAGE -> null
            else -> translationsRepository.loadTranslation(movie, language, onlyLocal = true)
          }
          return@map PersonDetailsItem.CreditsMovieItem(movie, image, isMy, isWatchlist, translation, moviesEnabled)
        }
        throw IllegalStateException()
      }
      .groupBy { it.getReleaseDate()?.year }
  }
}
