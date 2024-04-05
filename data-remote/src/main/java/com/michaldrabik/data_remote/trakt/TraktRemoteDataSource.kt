package com.michaldrabik.data_remote.trakt

import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.Ids
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.MovieCollection
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.PersonCredit
import com.michaldrabik.data_remote.trakt.model.SearchResult
import com.michaldrabik.data_remote.trakt.model.Season
import com.michaldrabik.data_remote.trakt.model.SeasonTranslation
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.Translation

/**
 * Fetch/post remote resources via unauthorized Trakt API
 */
interface TraktRemoteDataSource {

  // Auth

  suspend fun fetchAuthTokens(code: String): OAuthResponse

  suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse

  suspend fun revokeAuthTokens(token: String)

  // Shows

  suspend fun fetchShow(traktId: Long): Show

  suspend fun fetchShow(traktSlug: String): Show

  suspend fun fetchPopularShows(genres: String, networks: String): List<Show>

  suspend fun fetchTrendingShows(genres: String, networks: String, limit: Int): List<Show>

  suspend fun fetchAnticipatedShows(genres: String, networks: String): List<Show>

  suspend fun fetchRelatedShows(traktId: Long, addToLimit: Int): List<Show>

  suspend fun fetchShowTranslations(traktId: Long, code: String): List<Translation>

  suspend fun fetchNextEpisode(traktId: Long): Episode?

  suspend fun fetchSeasons(traktId: Long): List<Season>

  suspend fun fetchShowComments(traktId: Long, limit: Int): List<Comment>

  suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int,
  ): List<Comment>

  suspend fun fetchSeasonTranslations(
    showTraktId: Long,
    seasonNumber: Int,
    code: String
  ): List<SeasonTranslation>

  // Movies

  suspend fun fetchMovie(traktId: Long): Movie

  suspend fun fetchMovie(traktSlug: String): Movie

  suspend fun fetchPopularMovies(genres: String): List<Movie>

  suspend fun fetchTrendingMovies(genres: String, limit: Int): List<Movie>

  suspend fun fetchAnticipatedMovies(genres: String): List<Movie>

  suspend fun fetchRelatedMovies(traktId: Long, addToLimit: Int): List<Movie>

  suspend fun fetchMovieComments(traktId: Long, limit: Int): List<Comment>

  suspend fun fetchMovieTranslations(traktId: Long, code: String): List<Translation>

  suspend fun fetchMovieCollections(traktId: Long): List<MovieCollection>

  suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie>

  // People

  suspend fun fetchPersonIds(idType: String, id: String): Ids?

  suspend fun fetchPersonShowsCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit>

  suspend fun fetchPersonMoviesCredits(traktId: Long, type: TmdbPerson.Type): List<PersonCredit>

  // Search

  suspend fun fetchSearch(query: String, withMovies: Boolean): List<SearchResult>

  suspend fun fetchSearchId(idType: String, id: String): List<SearchResult>

  // Misc

  suspend fun fetchCommentReplies(commentId: Long): List<Comment>
}
