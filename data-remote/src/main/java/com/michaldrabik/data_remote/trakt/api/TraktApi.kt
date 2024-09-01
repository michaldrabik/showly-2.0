package com.michaldrabik.data_remote.trakt.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.Config.TRAKT_ANTICIPATED_SHOWS_LIMIT
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_ID
import com.michaldrabik.data_remote.Config.TRAKT_CLIENT_SECRET
import com.michaldrabik.data_remote.Config.TRAKT_REDIRECT_URL
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.data_remote.trakt.api.service.TraktAuthService
import com.michaldrabik.data_remote.trakt.api.service.TraktCommentsService
import com.michaldrabik.data_remote.trakt.api.service.TraktMoviesService
import com.michaldrabik.data_remote.trakt.api.service.TraktPeopleService
import com.michaldrabik.data_remote.trakt.api.service.TraktSearchService
import com.michaldrabik.data_remote.trakt.api.service.TraktShowsService
import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.Ids
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.MovieCollection
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.PersonCredit
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRevokeRequest
import java.lang.System.currentTimeMillis

internal class TraktApi(
  private val showsService: TraktShowsService,
  private val moviesService: TraktMoviesService,
  private val authService: TraktAuthService,
  private val commentsService: TraktCommentsService,
  private val searchService: TraktSearchService,
  private val peopleService: TraktPeopleService,
) : TraktRemoteDataSource {

  override suspend fun fetchShow(traktId: Long) = showsService.fetchShow(traktId)

  override suspend fun fetchShow(traktSlug: String) = showsService.fetchShow(traktSlug)

  override suspend fun fetchMovie(traktId: Long) = moviesService.fetchMovie(traktId)

  override suspend fun fetchMovie(traktSlug: String) = moviesService.fetchMovie(traktSlug)

  override suspend fun fetchPopularShows(
    genres: String,
    networks: String,
  ) = showsService.fetchPopularShows(genres, networks, Config.TRAKT_POPULAR_SHOWS_LIMIT)

  override suspend fun fetchPopularMovies(genres: String) = moviesService.fetchPopularMovies(genres)

  override suspend fun fetchTrendingShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> = showsService.fetchTrendingShows(genres, networks, limit).map { it.show!! }

  override suspend fun fetchTrendingMovies(
    genres: String,
    limit: Int,
  ) = moviesService.fetchTrendingMovies(genres, limit).map { it.movie!! }

  override suspend fun fetchAnticipatedShows(
    genres: String,
    networks: String,
  ): List<Show> = showsService.fetchAnticipatedShows(genres, networks, TRAKT_ANTICIPATED_SHOWS_LIMIT).map { it.show!! }

  override suspend fun fetchAnticipatedMovies(genres: String) =
    moviesService.fetchAnticipatedMovies(genres).map { it.movie!! }

  override suspend fun fetchRelatedShows(
    traktId: Long,
    addToLimit: Int,
  ) = showsService.fetchRelatedShows(traktId, Config.TRAKT_RELATED_SHOWS_LIMIT + addToLimit)

  override suspend fun fetchRelatedMovies(
    traktId: Long,
    addToLimit: Int,
  ) = moviesService.fetchRelatedMovies(traktId, Config.TRAKT_RELATED_MOVIES_LIMIT + addToLimit)

  override suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = showsService.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  override suspend fun fetchSearch(
    query: String,
    withMovies: Boolean,
  ) = if (withMovies) {
    searchService.fetchSearchResultsMovies(query)
  } else {
    searchService.fetchSearchResults(query)
  }

  override suspend fun fetchPersonIds(
    idType: String,
    id: String,
  ): Ids? {
    val result = searchService.fetchPersonIds(idType, id)
    if (result.isNotEmpty()) {
      return result.first().person?.ids
    }
    return null
  }

  override suspend fun fetchPersonShowsCredits(
    traktId: Long,
    type: TmdbPerson.Type,
  ): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "shows")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.show?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchPersonMoviesCredits(
    traktId: Long,
    type: TmdbPerson.Type,
  ): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "movies")
    val cast = result.cast ?: emptyList()
    val crew = result.crew?.values?.flatten()?.distinctBy { it.movie?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchSearchId(
    idType: String,
    id: String,
  ) = searchService.fetchSearchId(idType, id)

  override suspend fun fetchSeasons(traktId: Long) =
    showsService.fetchSeasons(traktId)
      .sortedByDescending { it.number }

  override suspend fun fetchShowComments(
    traktId: Long,
    limit: Int,
  ) = showsService.fetchShowComments(traktId, limit, currentTimeMillis())

  override suspend fun fetchMovieComments(
    traktId: Long,
    limit: Int,
  ) = moviesService.fetchMovieComments(traktId, limit, currentTimeMillis())

  override suspend fun fetchCommentReplies(commentId: Long) =
    commentsService.fetchCommentReplies(commentId, currentTimeMillis())

  override suspend fun fetchShowTranslations(
    traktId: Long,
    code: String,
  ) = showsService.fetchShowTranslations(traktId, code)

  override suspend fun fetchMovieTranslations(
    traktId: Long,
    code: String,
  ) = moviesService.fetchMovieTranslations(traktId, code)

  override suspend fun fetchSeasonTranslations(
    showTraktId: Long,
    seasonNumber: Int,
    code: String,
  ) = showsService.fetchSeasonTranslations(showTraktId, seasonNumber, code)

  override suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int,
  ): List<Comment> =
    try {
      showsService.fetchEpisodeComments(traktId, seasonNumber, episodeNumber, currentTimeMillis())
    } catch (t: Throwable) {
      emptyList()
    }

  override suspend fun fetchAuthTokens(code: String): OAuthResponse {
    val request = OAuthRequest(
      code,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL,
    )
    return authService.fetchOAuthToken(request)
  }

  override suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse {
    val request = OAuthRefreshRequest(
      refreshToken,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL,
    )
    return authService.refreshOAuthToken(request)
  }

  override suspend fun revokeAuthTokens(token: String) {
    val request = OAuthRevokeRequest(
      token,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
    )
    authService.revokeOAuthToken(request)
  }

  override suspend fun fetchMovieCollections(traktId: Long): List<MovieCollection> {
    val lists = moviesService.fetchMovieCollections(traktId)
    return lists.filter { it.privacy == "public" }
  }

  override suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie> {
    return moviesService.fetchMovieCollectionItems(collectionId)
      .sortedBy { it.rank }
      .map { it.movie }
  }
}
