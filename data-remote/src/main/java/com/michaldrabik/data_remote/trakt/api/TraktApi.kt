package com.michaldrabik.data_remote.trakt.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_ID
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_SECRET
import com.michaldrabik.data_remote.Config.TRAKT_REDIRECT_URL
import com.michaldrabik.data_remote.Config.TRAKT_SYNC_PAGE_LIMIT
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.trakt.api.service.TraktAuthService
import com.michaldrabik.data_remote.trakt.api.service.TraktCommentsService
import com.michaldrabik.data_remote.trakt.api.service.TraktMoviesService
import com.michaldrabik.data_remote.trakt.api.service.TraktPeopleService
import com.michaldrabik.data_remote.trakt.api.service.TraktSearchService
import com.michaldrabik.data_remote.trakt.api.service.TraktShowsService
import com.michaldrabik.data_remote.trakt.api.service.TraktSyncService
import com.michaldrabik.data_remote.trakt.api.service.TraktUsersService
import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.CustomList
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.Ids
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.PersonCredit
import com.michaldrabik.data_remote.trakt.model.Season
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncExportResult
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.data_remote.trakt.model.request.CommentRequest
import com.michaldrabik.data_remote.trakt.model.request.CreateListRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRevokeRequest
import com.michaldrabik.data_remote.trakt.model.request.RatingRequest
import com.michaldrabik.data_remote.trakt.model.request.RatingRequestValue
import java.lang.System.currentTimeMillis

class TraktApi(
  private val showsService: TraktShowsService,
  private val moviesService: TraktMoviesService,
  private val usersService: TraktUsersService,
  private val syncService: TraktSyncService,
  private val authService: TraktAuthService,
  private val commentsService: TraktCommentsService,
  private val searchService: TraktSearchService,
  private val peopleService: TraktPeopleService,
) {

  suspend fun fetchShow(traktId: Long) = showsService.fetchShow(traktId)

  suspend fun fetchShow(traktSlug: String) = showsService.fetchShow(traktSlug)

  suspend fun fetchMovie(traktId: Long) = moviesService.fetchMovie(traktId)

  suspend fun fetchMovie(traktSlug: String) = moviesService.fetchMovie(traktSlug)

  suspend fun fetchPopularShows(genres: String) = showsService.fetchPopularShows(genres)

  suspend fun fetchPopularMovies(genres: String) = moviesService.fetchPopularMovies(genres)

  suspend fun fetchTrendingShows(genres: String, limit: Int) = showsService.fetchTrendingShows(genres, limit).map { it.show!! }

  suspend fun fetchTrendingMovies(genres: String, limit: Int) = moviesService.fetchTrendingMovies(genres, limit).map { it.movie!! }

  suspend fun fetchAnticipatedShows(genres: String) = showsService.fetchAnticipatedShows(genres).map { it.show!! }

  suspend fun fetchAnticipatedMovies(genres: String) = moviesService.fetchAnticipatedMovies(genres).map { it.movie!! }

  suspend fun fetchRelatedShows(traktId: Long, addToLimit: Int) =
    showsService.fetchRelatedShows(traktId, Config.TRAKT_RELATED_SHOWS_LIMIT + addToLimit)

  suspend fun fetchRelatedMovies(traktId: Long, addToLimit: Int) =
    moviesService.fetchRelatedMovies(traktId, Config.TRAKT_RELATED_MOVIES_LIMIT + addToLimit)

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = showsService.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  suspend fun fetchSearch(query: String, withMovies: Boolean) =
    if (withMovies) searchService.fetchSearchResultsMovies(query)
    else searchService.fetchSearchResults(query)

  suspend fun fetchPersonIds(idType: String, id: String): Ids? {
    val result = searchService.fetchPersonIds(idType, id)
    if (result.isNotEmpty()) {
      return result.first().person?.ids
    }
    return null
  }

  suspend fun fetchPersonShowsCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "shows")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.show?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  suspend fun fetchPersonMoviesCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "movies")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.movie?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  suspend fun fetchSearchId(idType: String, id: String) =
    searchService.fetchSearchId(idType, id)

  suspend fun fetchSeasons(traktId: Long) =
    showsService.fetchSeasons(traktId)
      .sortedByDescending { it.number }

  suspend fun fetchShowComments(traktId: Long, limit: Int) =
    showsService.fetchShowComments(traktId, limit, currentTimeMillis())

  suspend fun fetchMovieComments(traktId: Long, limit: Int) =
    moviesService.fetchMovieComments(traktId, limit, currentTimeMillis())

  suspend fun fetchCommentReplies(commentId: Long) =
    commentsService.fetchCommentReplies(commentId, currentTimeMillis())

  suspend fun postComment(token: String, commentRequest: CommentRequest) =
    commentsService.postComment("Bearer $token", commentRequest)

  suspend fun postCommentReply(token: String, commentId: Long, commentRequest: CommentRequest) =
    commentsService.postCommentReply("Bearer $token", commentId, commentRequest)

  suspend fun deleteComment(token: String, commentId: Long) =
    commentsService.deleteComment("Bearer $token", commentId)

  suspend fun fetchShowTranslations(traktId: Long, code: String) =
    showsService.fetchShowTranslations(traktId, code)

  suspend fun fetchMovieTranslations(traktId: Long, code: String) =
    moviesService.fetchMovieTranslations(traktId, code)

  suspend fun fetchSeasonTranslations(showTraktId: Long, seasonNumber: Int, code: String) =
    showsService.fetchSeasonTranslations(showTraktId, seasonNumber, code)

  suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int
  ): List<Comment> = try {
    showsService.fetchEpisodeComments(traktId, seasonNumber, episodeNumber, currentTimeMillis())
  } catch (t: Throwable) {
    emptyList()
  }

  suspend fun fetchAuthTokens(code: String): OAuthResponse {
    val request = OAuthRequest(
      code,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return authService.fetchOAuthToken(request)
  }

  suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse {
    val request = OAuthRefreshRequest(
      refreshToken,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return authService.refreshOAuthToken(request)
  }

  suspend fun revokeAuthTokens(token: String) {
    val request = OAuthRevokeRequest(
      token,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET
    )
    authService.revokeOAuthToken(request)
  }

  suspend fun fetchMyProfile(token: String) =
    usersService.fetchMyProfile("Bearer $token")

  suspend fun fetchHiddenShows(token: String) =
    usersService.fetchHiddenShows("Bearer $token", pageLimit = 250)

  suspend fun postHiddenShows(
    token: String,
    shows: List<SyncExportItem> = emptyList()
  ) {
    usersService.postHiddenShows("Bearer $token", SyncExportRequest(shows = shows))
  }

  suspend fun postHiddenMovies(
    token: String,
    movies: List<SyncExportItem> = emptyList()
  ) {
    usersService.postHiddenMovies("Bearer $token", SyncExportRequest(movies = movies))
  }

  suspend fun fetchHiddenMovies(token: String) =
    usersService.fetchHiddenMovies("Bearer $token", pageLimit = 250)

  suspend fun fetchSyncWatchedShows(token: String, extended: String? = null) =
    syncService.fetchSyncWatched("Bearer $token", "shows", extended)

  suspend fun fetchSyncWatchedMovies(token: String, extended: String? = null) =
    syncService.fetchSyncWatched("Bearer $token", "movies", extended)

  suspend fun fetchSyncShowsWatchlist(token: String) = fetchSyncWatchlist(token, "shows")

  suspend fun fetchSyncMoviesWatchlist(token: String) = fetchSyncWatchlist(token, "movies")

  private suspend fun fetchSyncWatchlist(token: String, type: String): List<SyncItem> {
    var page = 1
    val results = mutableListOf<SyncItem>()

    do {
      val items = syncService.fetchSyncWatchlist("Bearer $token", type, page, TRAKT_SYNC_PAGE_LIMIT)
      results.addAll(items)
      page += 1
    } while (items.size >= TRAKT_SYNC_PAGE_LIMIT)

    return results
  }

  suspend fun fetchSyncLists(token: String) =
    usersService.fetchSyncLists("Bearer $token")

  suspend fun fetchSyncList(token: String, listId: Long) =
    usersService.fetchSyncList("Bearer $token", listId)

  suspend fun fetchSyncListItems(token: String, listId: Long, withMovies: Boolean): List<SyncItem> {
    var page = 1
    val results = mutableListOf<SyncItem>()
    val types = arrayListOf("show")
      .apply { if (withMovies) add("movie") }
      .joinToString(",")

    do {
      val items = usersService.fetchSyncListItems("Bearer $token", listId, types, page, TRAKT_SYNC_PAGE_LIMIT)
      results.addAll(items)
      page += 1
    } while (items.size >= TRAKT_SYNC_PAGE_LIMIT)

    return results
  }

  suspend fun postCreateList(token: String, name: String, description: String?): CustomList {
    val body = CreateListRequest(name, description)
    return usersService.postCreateList("Bearer $token", body)
  }

  suspend fun postUpdateList(token: String, customList: CustomList): CustomList {
    val body = CreateListRequest(customList.name, customList.description)
    return usersService.postUpdateList("Bearer $token", customList.ids.trakt, body)
  }

  suspend fun deleteList(token: String, listId: Long) {
    usersService.deleteList("Bearer $token", listId)
  }

  suspend fun postAddListItems(
    token: String,
    listTraktId: Long,
    showsIds: List<Long>,
    moviesIds: List<Long>
  ): SyncExportResult {
    val body = SyncExportRequest(
      shows = showsIds.map { SyncExportItem.create(it, null) },
      movies = moviesIds.map { SyncExportItem.create(it, null) }
    )
    return usersService.postAddListItems("Bearer $token", listTraktId, body)
  }

  suspend fun postRemoveListItems(
    token: String,
    listTraktId: Long,
    showsIds: List<Long>,
    moviesIds: List<Long>
  ): SyncExportResult {
    val body = SyncExportRequest(
      shows = showsIds.map { SyncExportItem.create(it, null) },
      movies = moviesIds.map { SyncExportItem.create(it, null) }
    )
    return usersService.postRemoveListItems("Bearer $token", listTraktId, body)
  }

  suspend fun postSyncWatchlist(token: String, request: SyncExportRequest) =
    syncService.postSyncWatchlist("Bearer $token", request)

  suspend fun postSyncWatched(token: String, request: SyncExportRequest) =
    syncService.postSyncWatched("Bearer $token", request)

  suspend fun postDeleteProgress(token: String, request: SyncExportRequest) =
    syncService.deleteHistory("Bearer $token", request)

  suspend fun postDeleteWatchlist(token: String, request: SyncExportRequest) =
    syncService.deleteWatchlist("Bearer $token", request)

  suspend fun deleteHiddenShow(token: String, request: SyncExportRequest) =
    usersService.deleteHidden("Bearer $token", "progress_watched", request)

  suspend fun deleteHiddenMovie(token: String, request: SyncExportRequest) =
    usersService.deleteHidden("Bearer $token", "calendar", request)

  suspend fun deleteRating(token: String, show: Show) {
    val requestValue = RatingRequestValue(0, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  suspend fun deleteRating(token: String, movie: Movie) {
    val requestValue = RatingRequestValue(0, movie.ids)
    val body = RatingRequest(movies = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  suspend fun deleteRating(token: String, episode: Episode) {
    val requestValue = RatingRequestValue(0, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  suspend fun deleteRating(token: String, season: Season) {
    val requestValue = RatingRequestValue(0, season.ids)
    val body = RatingRequest(seasons = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, movie: Movie, rating: Int) {
    val requestValue = RatingRequestValue(rating, movie.ids)
    val body = RatingRequest(movies = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, show: Show, rating: Int) {
    val requestValue = RatingRequestValue(rating, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, episode: Episode, rating: Int) {
    val requestValue = RatingRequestValue(rating, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, season: Season, rating: Int) {
    val requestValue = RatingRequestValue(rating, season.ids)
    val body = RatingRequest(seasons = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  suspend fun fetchShowsRatings(token: String) =
    syncService.fetchShowsRatings("Bearer $token")

  suspend fun fetchMoviesRatings(token: String) =
    syncService.fetchMoviesRatings("Bearer $token")

  suspend fun fetchEpisodesRatings(token: String) =
    syncService.fetchEpisodesRatings("Bearer $token")

  suspend fun fetchSeasonsRatings(token: String) =
    syncService.fetchSeasonsRatings("Bearer $token")
}
