package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.Config.TRAKT_CLIENT_ID
import com.michaldrabik.network.Config.TRAKT_CLIENT_SECRET
import com.michaldrabik.network.Config.TRAKT_REDIRECT_URL
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.OAuthResponse
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.network.trakt.model.request.OAuthRequest
import com.michaldrabik.network.trakt.model.request.OAuthRevokeRequest
import com.michaldrabik.network.trakt.model.request.RatingRequest
import com.michaldrabik.network.trakt.model.request.RatingRequestShow

class TraktApi(private val service: TraktService) {

  suspend fun fetchShow(traktId: Long) = service.fetchShow(traktId)

  suspend fun fetchTrendingShows() = service.fetchTrendingShows().map { it.show }

  suspend fun fetchAnticipatedShows() = service.fetchAnticipatedShows().map { it.show }

  suspend fun fetchRelatedShows(traktId: Long) = service.fetchRelatedShows(traktId)

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = service.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  suspend fun fetchShowsSearch(query: String) =
    service.fetchSearchResults(query)
      .sortedWith(compareBy({ it.score }, { it.show.votes }))
      .reversed()
      .map { it.show }

  suspend fun fetchSeasons(traktId: Long) =
    service.fetchSeasons(traktId)
      .filter { it.number != 0 } // Filtering out "special" seasons
      .sortedByDescending { it.number }

  suspend fun fetchShowComments(traktId: Long, limit: Int) =
    service.fetchShowComments(traktId, limit)

  suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int
  ): List<Comment> = try {
    service.fetchEpisodeComments(traktId, seasonNumber, episodeNumber)
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
    return service.fetchOAuthToken(request)
  }

  suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse {
    val request = OAuthRefreshRequest(
      refreshToken,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return service.refreshOAuthToken(request)
  }

  suspend fun revokeAuthTokens(token: String) {
    val request = OAuthRevokeRequest(
      token,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET
    )
    service.revokeOAuthToken(request)
  }

  suspend fun fetchMyProfile(token: String) =
    service.fetchMyProfile("Bearer $token")

  suspend fun fetchSyncWatched(token: String, extended: String? = null) =
    service.fetchSyncWatched("Bearer $token", extended)

  suspend fun fetchSyncWatchlist(token: String) =
    service.fetchSyncWatchlist("Bearer $token")

  suspend fun postSyncWatchlist(token: String, request: SyncExportRequest) =
    service.postSyncWatchlist("Bearer $token", request)

  suspend fun postSyncWatched(token: String, request: SyncExportRequest) =
    service.postSyncWatched("Bearer $token", request)

  suspend fun postRating(token: String, show: Show, rating: Int) {
    val requestShow = RatingRequestShow(rating, show.ids)
    val body = RatingRequest(listOf(requestShow))
    service.postRating("Bearer $token", body)
  }

  suspend fun fetchShowsRatings(token: String) =
    service.fetchShowsRatings("Bearer $token")
}
