package com.michaldrabik.data_remote.trakt.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_ID
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_SECRET
import com.michaldrabik.data_remote.Config.TRAKT_REDIRECT_URL
import com.michaldrabik.data_remote.Config.TRAKT_SYNC_PAGE_LIMIT
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
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

internal class TraktApi(
  private val showsService: TraktShowsService,
  private val moviesService: TraktMoviesService,
  private val usersService: TraktUsersService,
  private val syncService: TraktSyncService,
  private val authService: TraktAuthService,
  private val commentsService: TraktCommentsService,
  private val searchService: TraktSearchService,
  private val peopleService: TraktPeopleService,
) : TraktRemoteDataSource {

  override suspend fun fetchShow(traktId: Long) = showsService.fetchShow(traktId)

  override suspend fun fetchShow(traktSlug: String) = showsService.fetchShow(traktSlug)

  override suspend fun fetchMovie(traktId: Long) = moviesService.fetchMovie(traktId)

  override suspend fun fetchMovie(traktSlug: String) = moviesService.fetchMovie(traktSlug)

  override suspend fun fetchPopularShows(genres: String) = showsService.fetchPopularShows(genres)

  override suspend fun fetchPopularMovies(genres: String) = moviesService.fetchPopularMovies(genres)

  override suspend fun fetchTrendingShows(genres: String, limit: Int) = showsService.fetchTrendingShows(genres, limit).map { it.show!! }

  override suspend fun fetchTrendingMovies(genres: String, limit: Int) = moviesService.fetchTrendingMovies(genres, limit).map { it.movie!! }

  override suspend fun fetchAnticipatedShows(genres: String) = showsService.fetchAnticipatedShows(genres).map { it.show!! }

  override suspend fun fetchAnticipatedMovies(genres: String) = moviesService.fetchAnticipatedMovies(genres).map { it.movie!! }

  override suspend fun fetchRelatedShows(traktId: Long, addToLimit: Int) =
    showsService.fetchRelatedShows(traktId, Config.TRAKT_RELATED_SHOWS_LIMIT + addToLimit)

  override suspend fun fetchRelatedMovies(traktId: Long, addToLimit: Int) =
    moviesService.fetchRelatedMovies(traktId, Config.TRAKT_RELATED_MOVIES_LIMIT + addToLimit)

  override suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = showsService.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  override suspend fun fetchSearch(query: String, withMovies: Boolean) =
    if (withMovies) searchService.fetchSearchResultsMovies(query)
    else searchService.fetchSearchResults(query)

  override suspend fun fetchPersonIds(idType: String, id: String): Ids? {
    val result = searchService.fetchPersonIds(idType, id)
    if (result.isNotEmpty()) {
      return result.first().person?.ids
    }
    return null
  }

  override suspend fun fetchPersonShowsCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "shows")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.show?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchPersonMoviesCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "movies")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.movie?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchSearchId(idType: String, id: String) =
    searchService.fetchSearchId(idType, id)

  override suspend fun fetchSeasons(traktId: Long) =
    showsService.fetchSeasons(traktId)
      .sortedByDescending { it.number }

  override suspend fun fetchShowComments(traktId: Long, limit: Int) =
    showsService.fetchShowComments(traktId, limit, currentTimeMillis())

  override suspend fun fetchMovieComments(traktId: Long, limit: Int) =
    moviesService.fetchMovieComments(traktId, limit, currentTimeMillis())

  override suspend fun fetchCommentReplies(commentId: Long) =
    commentsService.fetchCommentReplies(commentId, currentTimeMillis())

  override suspend fun postComment(token: String, commentRequest: CommentRequest) =
    commentsService.postComment("Bearer $token", commentRequest)

  override suspend fun postCommentReply(token: String, commentId: Long, commentRequest: CommentRequest) =
    commentsService.postCommentReply("Bearer $token", commentId, commentRequest)

  override suspend fun deleteComment(token: String, commentId: Long) =
    commentsService.deleteComment("Bearer $token", commentId)

  override suspend fun fetchShowTranslations(traktId: Long, code: String) =
    showsService.fetchShowTranslations(traktId, code)

  override suspend fun fetchMovieTranslations(traktId: Long, code: String) =
    moviesService.fetchMovieTranslations(traktId, code)

  override suspend fun fetchSeasonTranslations(showTraktId: Long, seasonNumber: Int, code: String) =
    showsService.fetchSeasonTranslations(showTraktId, seasonNumber, code)

  override suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int
  ): List<Comment> = try {
    showsService.fetchEpisodeComments(traktId, seasonNumber, episodeNumber, currentTimeMillis())
  } catch (t: Throwable) {
    emptyList()
  }

  override suspend fun fetchAuthTokens(code: String): OAuthResponse {
    val request = OAuthRequest(
      code,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return authService.fetchOAuthToken(request)
  }

  override suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse {
    val request = OAuthRefreshRequest(
      refreshToken,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return authService.refreshOAuthToken(request)
  }

  override suspend fun revokeAuthTokens(token: String) {
    val request = OAuthRevokeRequest(
      token,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET
    )
    authService.revokeOAuthToken(request)
  }

  override suspend fun fetchMyProfile(token: String) =
    usersService.fetchMyProfile("Bearer $token")

  override suspend fun fetchHiddenShows(token: String) =
    usersService.fetchHiddenShows("Bearer $token", pageLimit = 250)

  override suspend fun postHiddenShows(
    token: String,
    shows: List<SyncExportItem>
  ) {
    usersService.postHiddenShows("Bearer $token", SyncExportRequest(shows = shows))
  }

  override suspend fun postHiddenMovies(
    token: String,
    movies: List<SyncExportItem>
  ) {
    usersService.postHiddenMovies("Bearer $token", SyncExportRequest(movies = movies))
  }

  override suspend fun fetchHiddenMovies(token: String) =
    usersService.fetchHiddenMovies("Bearer $token", pageLimit = 250)

  override suspend fun fetchSyncWatchedShows(token: String, extended: String?) =
    syncService.fetchSyncWatched("Bearer $token", "shows", extended)

  override suspend fun fetchSyncWatchedMovies(token: String, extended: String?) =
    syncService.fetchSyncWatched("Bearer $token", "movies", extended)

  override suspend fun fetchSyncShowsWatchlist(token: String) = fetchSyncWatchlist(token, "shows")

  override suspend fun fetchSyncMoviesWatchlist(token: String) = fetchSyncWatchlist(token, "movies")

  override suspend fun fetchSyncWatchlist(token: String, type: String): List<SyncItem> {
    var page = 1
    val results = mutableListOf<SyncItem>()

    do {
      val items = syncService.fetchSyncWatchlist("Bearer $token", type, page, TRAKT_SYNC_PAGE_LIMIT)
      results.addAll(items)
      page += 1
    } while (items.size >= TRAKT_SYNC_PAGE_LIMIT)

    return results
  }

  override suspend fun fetchSyncLists(token: String) =
    usersService.fetchSyncLists("Bearer $token")

  override suspend fun fetchSyncList(token: String, listId: Long) =
    usersService.fetchSyncList("Bearer $token", listId)

  override suspend fun fetchSyncListItems(token: String, listId: Long, withMovies: Boolean): List<SyncItem> {
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

  override suspend fun postCreateList(token: String, name: String, description: String?): CustomList {
    val body = CreateListRequest(name, description)
    return usersService.postCreateList("Bearer $token", body)
  }

  override suspend fun postUpdateList(token: String, customList: CustomList): CustomList {
    val body = CreateListRequest(customList.name, customList.description)
    return usersService.postUpdateList("Bearer $token", customList.ids.trakt, body)
  }

  override suspend fun deleteList(token: String, listId: Long) {
    usersService.deleteList("Bearer $token", listId)
  }

  override suspend fun postAddListItems(
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

  override suspend fun postRemoveListItems(
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

  override suspend fun postSyncWatchlist(token: String, request: SyncExportRequest) =
    syncService.postSyncWatchlist("Bearer $token", request)

  override suspend fun postSyncWatched(token: String, request: SyncExportRequest) =
    syncService.postSyncWatched("Bearer $token", request)

  override suspend fun postDeleteProgress(token: String, request: SyncExportRequest) =
    syncService.deleteHistory("Bearer $token", request)

  override suspend fun postDeleteWatchlist(token: String, request: SyncExportRequest) =
    syncService.deleteWatchlist("Bearer $token", request)

  override suspend fun deleteHiddenShow(token: String, request: SyncExportRequest) =
    usersService.deleteHidden("Bearer $token", "progress_watched", request)

  override suspend fun deleteHiddenMovie(token: String, request: SyncExportRequest) =
    usersService.deleteHidden("Bearer $token", "calendar", request)

  override suspend fun deleteRating(token: String, show: Show) {
    val requestValue = RatingRequestValue(0, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  override suspend fun deleteRating(token: String, movie: Movie) {
    val requestValue = RatingRequestValue(0, movie.ids)
    val body = RatingRequest(movies = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  override suspend fun deleteRating(token: String, episode: Episode) {
    val requestValue = RatingRequestValue(0, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  override suspend fun deleteRating(token: String, season: Season) {
    val requestValue = RatingRequestValue(0, season.ids)
    val body = RatingRequest(seasons = listOf(requestValue))
    syncService.postRemoveRating("Bearer $token", body)
  }

  override suspend fun postRating(token: String, movie: Movie, rating: Int) {
    val requestValue = RatingRequestValue(rating, movie.ids)
    val body = RatingRequest(movies = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  override suspend fun postRating(token: String, show: Show, rating: Int) {
    val requestValue = RatingRequestValue(rating, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  override suspend fun postRating(token: String, episode: Episode, rating: Int) {
    val requestValue = RatingRequestValue(rating, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  override suspend fun postRating(token: String, season: Season, rating: Int) {
    val requestValue = RatingRequestValue(rating, season.ids)
    val body = RatingRequest(seasons = listOf(requestValue))
    syncService.postRating("Bearer $token", body)
  }

  override suspend fun fetchShowsRatings(token: String) =
    syncService.fetchShowsRatings("Bearer $token")

  override suspend fun fetchMoviesRatings(token: String) =
    syncService.fetchMoviesRatings("Bearer $token")

  override suspend fun fetchEpisodesRatings(token: String) =
    syncService.fetchEpisodesRatings("Bearer $token")

  override suspend fun fetchSeasonsRatings(token: String) =
    syncService.fetchSeasonsRatings("Bearer $token")
}
