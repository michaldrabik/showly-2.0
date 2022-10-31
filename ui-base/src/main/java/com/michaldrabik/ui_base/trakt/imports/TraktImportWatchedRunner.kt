package com.michaldrabik.ui_base.trakt.imports

import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.ArchiveShow
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.MyMovie
import com.michaldrabik.data_local.database.model.MyShow
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktImportWatchedRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val transactions: TransactionsProvider,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager,
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")

    var syncedCount = 0
    checkAuthorization()

    resetRetries()
    syncedCount += runShows()

    resetRetries()
    syncedCount += runMovies()

    Timber.d("Finished with success.")

    return syncedCount
  }

  private suspend fun runShows(): Int =
    try {
      importWatchedShows()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runShows HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runShows()
      } else {
        throw error
      }
    }

  private suspend fun runMovies(): Int {
    if (!settingsRepository.isMoviesEnabled) {
      Timber.d("Movies are disabled. Exiting...")
      return 0
    }
    return try {
      importWatchedMovies()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runMovies HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runMovies()
      } else {
        throw error
      }
    }
  }

  private suspend fun importWatchedShows(): Int {
    Timber.d("Importing watched shows...")
    val syncResults = remoteSource.trakt.fetchSyncWatchedShows("full")
      .filter { it.show != null }
      .distinctBy { it.show?.ids?.trakt }

    Timber.d("Importing hidden shows...")
    val hiddenShows = remoteSource.trakt.fetchHiddenShows()
    hiddenShows.forEach { hiddenShow ->
      hiddenShow.show?.let {
        val show = mappers.show.fromNetwork(it)
        val dbShow = mappers.show.toDatabase(show)
        val archiveShow = ArchiveShow.fromTraktId(show.traktId, hiddenShow.hiddenAtMillis())
        transactions.withTransaction {
          with(localSource) {
            shows.upsert(listOf(dbShow))
            archiveShows.insert(archiveShow)
            myShows.deleteById(show.traktId)
            watchlistShows.deleteById(show.traktId)
          }
        }
      }
    }

    val myShowsIds = localSource.myShows.getAllTraktIds()
    val watchlistShowsIds = localSource.watchlistShows.getAllTraktIds()
    val hiddenShowsIds = localSource.archiveShows.getAllTraktIds()
    val traktSyncLogs = localSource.traktSyncLog.getAllShows()

    syncResults
      .forEachIndexed { index, result ->
        val showUi = mappers.show.fromNetwork(result.show!!)
        progressListener?.invoke(showUi.title, index, syncResults.size)

        Timber.d("Processing \'${showUi.title}\'...")

        val log = traktSyncLogs.firstOrNull { it.idTrakt == result.show?.ids?.trakt }
        if (result.lastUpdateMillis() == (log?.syncedAt ?: 0)) {
          Timber.d("Nothing changed in \'${result.show!!.title}\'. Skipping...")
          return@forEachIndexed
        }

        try {
          val showId = result.show!!.ids!!.trakt!!
          val (seasons, episodes) = loadSeasons(showId, result)

          transactions.withTransaction {
            if (showId !in myShowsIds && showId !in hiddenShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)

              val myShow = MyShow.fromTraktId(
                traktId = showDb.idTrakt,
                createdAt = result.lastWatchedMillis(),
                updatedAt = result.lastWatchedMillis(),
                watchedAt = result.lastWatchedMillis()
              )
              localSource.shows.upsert(listOf(showDb))
              localSource.myShows.insert(listOf(myShow))

              loadImage(show)

              if (showId in watchlistShowsIds) {
                localSource.watchlistShows.deleteById(showId)
              }
            }
            localSource.seasons.upsert(seasons)
            localSource.episodes.upsert(episodes)

            localSource.myShows.updateWatchedAt(showId, result.lastWatchedMillis())
            localSource.traktSyncLog.upsertShow(showId, result.lastUpdateMillis())
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
          Logger.record(error, "TraktImportWatchedRunner::importWatchedShows()")
        }
      }

    return syncResults.size
  }

  private suspend fun loadSeasons(showId: Long, syncItem: SyncItem): Pair<List<Season>, List<Episode>> {
    val remoteSeasons = remoteSource.trakt.fetchSeasons(showId)
    val localSeasonsIds = localSource.seasons.getAllWatchedIdsForShows(listOf(showId))
    val localEpisodesIds = localSource.episodes.getAllWatchedIdsForShows(listOf(showId))

    val seasons = remoteSeasons
      .filterNot { localSeasonsIds.contains(it.ids?.trakt) }
      .map { mappers.season.fromNetwork(it) }
      .map { remoteSeason ->
        val isWatched = syncItem.seasons?.any {
          it.number == remoteSeason.number && it.episodes?.size == remoteSeason.episodes.size
        } ?: false
        mappers.season.toDatabase(remoteSeason, IdTrakt(showId), isWatched)
      }

    val episodes = remoteSeasons.flatMap { season ->
      season.episodes
        ?.filterNot { localEpisodesIds.contains(it.ids?.trakt) }
        ?.map { episode ->
          val syncEpisode = syncItem.seasons
            ?.find { it.number == season.number }?.episodes
            ?.find { it.number == episode.number }

          val isWatched = syncEpisode != null
          val watchedAt = syncEpisode?.last_watched_at?.toZonedDateTime()

          val seasonDb = mappers.season.fromNetwork(season)
          val episodeDb = mappers.episode.fromNetwork(episode)
          mappers.episode.toDatabase(episodeDb, seasonDb, IdTrakt(showId), isWatched, watchedAt)
        } ?: emptyList()
    }

    return Pair(seasons, episodes)
  }

  private suspend fun importWatchedMovies(): Int {
    Timber.d("Importing watched movies...")

    val syncResults = remoteSource.trakt.fetchSyncWatchedMovies("full")
      .filter { it.movie != null }
      .distinctBy { it.movie?.ids?.trakt }

    Timber.d("Importing hidden movies...")

    val hiddenMovies = remoteSource.trakt.fetchHiddenMovies()
    hiddenMovies.forEach { hiddenMovie ->
      hiddenMovie.movie?.let {
        val movie = mappers.movie.fromNetwork(it)
        val dbMovie = mappers.movie.toDatabase(movie)
        val archiveMovie = ArchiveMovie.fromTraktId(movie.traktId, hiddenMovie.hiddenAtMillis())
        transactions.withTransaction {
          with(localSource) {
            movies.upsert(listOf(dbMovie))
            archiveMovies.insert(archiveMovie)
            myMovies.deleteById(movie.traktId)
            watchlistMovies.deleteById(movie.traktId)
          }
        }
      }
    }

    val myMoviesIds = localSource.myMovies.getAllTraktIds()
    val watchlistMoviesIds = localSource.watchlistMovies.getAllTraktIds()
    val hiddenMoviesIds = localSource.archiveMovies.getAllTraktIds()

    syncResults
      .forEachIndexed { index, result ->
        Timber.d("Processing \'${result.movie!!.title}\'...")
        val movieUi = mappers.movie.fromNetwork(result.movie!!)
        progressListener?.invoke(movieUi.title, index, syncResults.size)

        try {
          val movieId = result.movie!!.ids!!.trakt!!

          transactions.withTransaction {
            if (movieId !in myMoviesIds && movieId !in hiddenMoviesIds) {
              val movie = mappers.movie.fromNetwork(result.movie!!)
              val movieDb = mappers.movie.toDatabase(movie)

              val myMovie = MyMovie.fromTraktId(movieDb.idTrakt, result.lastWatchedMillis())
              localSource.movies.upsert(listOf(movieDb))
              localSource.myMovies.insert(listOf(myMovie))

              loadImage(movie)

              if (movieId in watchlistMoviesIds) {
                localSource.watchlistMovies.deleteById(movieId)
              }
            }
          }
        } catch (error: Throwable) {
          Timber.w("Processing \'${result.movie!!.title}\' failed. Skipping...")
          Logger.record(error, "TraktImportWatchedRunner::importWatchedMovies()")
        }
      }

    return syncResults.size
  }

  private suspend fun loadImage(show: Show) {
    try {
      showImagesProvider.loadRemoteImage(show, FANART)
    } catch (error: Throwable) {
      Timber.w(error)
      // Ignore image for now. It will be fetched later if needed.
    }
  }

  private suspend fun loadImage(movie: Movie) {
    try {
      movieImagesProvider.loadRemoteImage(movie, FANART)
    } catch (error: Throwable) {
      Timber.w(error)
      // Ignore image for now. It will be fetched later if needed.
    }
  }
}
