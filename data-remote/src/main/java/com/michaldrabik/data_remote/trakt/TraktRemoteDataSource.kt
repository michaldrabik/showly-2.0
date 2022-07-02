package com.michaldrabik.data_remote.trakt

import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.CustomList
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.HiddenItem
import com.michaldrabik.data_remote.trakt.model.Ids
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.PersonCredit
import com.michaldrabik.data_remote.trakt.model.RatingResultEpisode
import com.michaldrabik.data_remote.trakt.model.RatingResultMovie
import com.michaldrabik.data_remote.trakt.model.RatingResultSeason
import com.michaldrabik.data_remote.trakt.model.RatingResultShow
import com.michaldrabik.data_remote.trakt.model.SearchResult
import com.michaldrabik.data_remote.trakt.model.Season
import com.michaldrabik.data_remote.trakt.model.SeasonTranslation
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncExportResult
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.data_remote.trakt.model.Translation
import com.michaldrabik.data_remote.trakt.model.User
import com.michaldrabik.data_remote.trakt.model.request.CommentRequest
import retrofit2.Response

/**
 * Fetch/post remote resources via Trakt API
 */
interface TraktRemoteDataSource {
  suspend fun fetchShow(traktId: Long): Show

  suspend fun fetchShow(traktSlug: String): Show

  suspend fun fetchMovie(traktId: Long): Movie

  suspend fun fetchMovie(traktSlug: String): Movie

  suspend fun fetchPopularShows(genres: String, networks: String): List<Show>

  suspend fun fetchPopularMovies(genres: String): List<Movie>

  suspend fun fetchTrendingShows(genres: String, networks: String, limit: Int): List<Show>

  suspend fun fetchTrendingMovies(genres: String, limit: Int): List<Movie>

  suspend fun fetchAnticipatedShows(genres: String, networks: String): List<Show>

  suspend fun fetchAnticipatedMovies(genres: String): List<Movie>

  suspend fun fetchRelatedShows(traktId: Long, addToLimit: Int): List<Show>

  suspend fun fetchRelatedMovies(traktId: Long, addToLimit: Int): List<Movie>

  suspend fun fetchNextEpisode(traktId: Long): Episode?

  suspend fun fetchSearch(query: String, withMovies: Boolean): List<SearchResult>

  suspend fun fetchPersonIds(idType: String, id: String): Ids?

  suspend fun fetchPersonShowsCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit>

  suspend fun fetchPersonMoviesCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit>

  suspend fun fetchSearchId(idType: String, id: String): List<SearchResult>

  suspend fun fetchSeasons(traktId: Long): List<Season>

  suspend fun fetchShowComments(traktId: Long, limit: Int): List<Comment>

  suspend fun fetchMovieComments(traktId: Long, limit: Int): List<Comment>

  suspend fun fetchCommentReplies(commentId: Long): List<Comment>

  suspend fun postComment(commentRequest: CommentRequest): Comment

  suspend fun postCommentReply(commentId: Long, commentRequest: CommentRequest): Comment

  suspend fun deleteComment(commentId: Long): Response<Any>

  suspend fun fetchShowTranslations(traktId: Long, code: String): List<Translation>

  suspend fun fetchMovieTranslations(traktId: Long, code: String): List<Translation>

  suspend fun fetchSeasonTranslations(showTraktId: Long, seasonNumber: Int, code: String): List<SeasonTranslation>

  suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int
  ): List<Comment>

  suspend fun fetchAuthTokens(code: String): OAuthResponse

  suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse

  suspend fun revokeAuthTokens(token: String)

  suspend fun fetchMyProfile(): User

  suspend fun fetchHiddenShows(): List<HiddenItem>

  suspend fun postHiddenShows(shows: List<SyncExportItem> = emptyList())

  suspend fun postHiddenMovies(movies: List<SyncExportItem> = emptyList())

  suspend fun fetchHiddenMovies(): List<HiddenItem>

  suspend fun fetchSyncWatchedShows(extended: String? = null): List<SyncItem>

  suspend fun fetchSyncWatchedMovies(extended: String? = null): List<SyncItem>

  suspend fun fetchSyncShowsWatchlist(): List<SyncItem>

  suspend fun fetchSyncMoviesWatchlist(): List<SyncItem>

  suspend fun fetchSyncWatchlist(type: String): List<SyncItem>

  suspend fun fetchSyncLists(): List<CustomList>

  suspend fun fetchSyncList(listId: Long): CustomList

  suspend fun fetchSyncListItems(listId: Long, withMovies: Boolean): List<SyncItem>

  suspend fun postCreateList(name: String, description: String?): CustomList

  suspend fun postUpdateList(customList: CustomList): CustomList

  suspend fun deleteList(listId: Long)

  suspend fun postAddListItems(
    listTraktId: Long,
    showsIds: List<Long>,
    moviesIds: List<Long>
  ): SyncExportResult

  suspend fun postRemoveListItems(
    listTraktId: Long,
    showsIds: List<Long>,
    moviesIds: List<Long>
  ): SyncExportResult

  suspend fun postSyncWatchlist(request: SyncExportRequest): SyncExportResult

  suspend fun postSyncWatched(request: SyncExportRequest): SyncExportResult

  suspend fun postDeleteProgress(request: SyncExportRequest): SyncExportResult

  suspend fun postDeleteWatchlist(request: SyncExportRequest): SyncExportResult

  suspend fun deleteHiddenShow(request: SyncExportRequest): SyncExportResult

  suspend fun deleteHiddenMovie(request: SyncExportRequest): SyncExportResult

  suspend fun deleteRating(show: Show)

  suspend fun deleteRating(movie: Movie)

  suspend fun deleteRating(episode: Episode)

  suspend fun deleteRating(season: Season)

  suspend fun postRating(movie: Movie, rating: Int)

  suspend fun postRating(show: Show, rating: Int)

  suspend fun postRating(episode: Episode, rating: Int)

  suspend fun postRating(season: Season, rating: Int)

  suspend fun fetchShowsRatings(): List<RatingResultShow>

  suspend fun fetchMoviesRatings(): List<RatingResultMovie>

  suspend fun fetchEpisodesRatings(): List<RatingResultEpisode>

  suspend fun fetchSeasonsRatings(): List<RatingResultSeason>
}
