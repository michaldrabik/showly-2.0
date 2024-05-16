package com.michaldrabik.repository

import android.content.SharedPreferences
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.EpisodeTranslation
import com.michaldrabik.data_local.database.model.MovieTranslation
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.data_local.database.model.TranslationsMoviesSyncLog
import com.michaldrabik.data_local.database.model.TranslationsSyncLog
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.Translation as TraktTranslation
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository.Key.LOCALE
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TranslationsRepository @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  fun getLocale() = miscPreferences.getString(LOCALE, null)?.let { AppLocale.fromCode(it) } ?: AppLocale.default()

  suspend fun loadAllShowsLocal(locale: AppLocale = AppLocale.default()): Map<Long, Translation> {
    val local = localSource.showTranslations.getAll(locale.language.code, locale.country.code)
    return local.associate {
      Pair(it.idTrakt, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadAllMoviesLocal(locale: AppLocale = AppLocale.default()): Map<Long, Translation> {
    val local = localSource.movieTranslations.getAll(locale.language.code, locale.country.code)
    return local.associate {
      Pair(it.idTrakt, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadTranslation(
    show: Show,
    locale: AppLocale = AppLocale.default(),
    onlyLocal: Boolean = false,
  ): Translation? {
    val language = locale.language.code
    val country = locale.country.code

    val local = localSource.showTranslations.getById(show.traktId, language, country)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = localSource.translationsShowsSyncLog.getById(show.traktId)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      findTranslation(remoteSource.trakt.fetchShowTranslations(show.traktId, language), language, country)
    } catch (_: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = ShowTranslation.fromTraktId(
      show.traktId,
      translation.title,
      language,
      country,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      localSource.showTranslations.insertSingle(translationDb)
    }
    localSource.translationsShowsSyncLog.upsert(TranslationsSyncLog(show.traktId, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    movie: Movie,
    locale: AppLocale = AppLocale.default(),
    onlyLocal: Boolean = false,
  ): Translation? {
    val language = locale.language.code
    val country = locale.country.code

    val local = localSource.movieTranslations.getById(movie.traktId, language, country)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = localSource.translationsMoviesSyncLog.getById(movie.traktId)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      findTranslation(remoteSource.trakt.fetchMovieTranslations(movie.traktId, language), language, country)
    } catch (error: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = MovieTranslation.fromTraktId(
      movie.traktId,
      translation.title,
      language,
      country,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      localSource.movieTranslations.insertSingle(translationDb)
    }
    localSource.translationsMoviesSyncLog.upsert(TranslationsMoviesSyncLog(movie.traktId, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    episode: Episode,
    showId: IdTrakt,
    locale: AppLocale = AppLocale.default(),
    onlyLocal: Boolean = false,
  ): Translation? {
    val language = locale.language.code
    val country = locale.country.code

    val nowMillis = nowUtcMillis()
    val local = localSource.episodesTranslations.getById(episode.ids.trakt.id, showId.id, language, country)
    local?.let {
      val isCacheValid = nowMillis - it.updatedAt < ConfigVariant.TRANSLATION_SYNC_EPISODE_COOLDOWN
      if (it.title.isNotBlank() && it.overview.isNotBlank()) {
        return mappers.translation.fromDatabase(it)
      }
      if ((it.title.isNotBlank() || it.overview.isNotBlank()) && (isCacheValid || onlyLocal)) {
        return mappers.translation.fromDatabase(it)
      }
    }

    if (onlyLocal) return null

    val remoteTranslations = remoteSource.trakt.fetchSeasonTranslations(showId.id, episode.season, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslations
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          traktEpisodeId = item.ids.trakt.id,
          traktShowId = showId.id,
          title = item.title,
          overview = item.overview,
          language = language,
          country = country,
          createdAt = nowMillis
        )
        localSource.episodesTranslations.insertSingle(dbItem)
      }

    findTranslation(episode.ids.trakt.id, remoteTranslations, language, country)
      ?.let {
        return Translation(it.title, it.overview, it.language, it.country)
      }

    return null
  }

  suspend fun loadTranslations(
    season: Season,
    showId: IdTrakt,
    locale: AppLocale = AppLocale.default(),
  ): List<SeasonTranslation> {
    val language = locale.language.code
    val country = locale.country.code

    val episodes = season.episodes.toList()
    val episodesIds = season.episodes.map { it.ids.trakt.id }

    val local = localSource.episodesTranslations.getByIds(episodesIds, showId.id, language, country)
    val hasAllTranslated = local.isNotEmpty() && local.all { it.title.isNotBlank() && it.overview.isNotBlank() }
    val isCacheValid = local.isNotEmpty() && nowUtcMillis() - local.first().updatedAt < ConfigVariant.TRANSLATION_SYNC_EPISODE_COOLDOWN

    if (hasAllTranslated || isCacheValid) {
      return episodes.map { episode ->
        val translation = local.find { it.idTrakt == episode.ids.trakt.id }
        SeasonTranslation(
          ids = episode.ids.copy(),
          title = translation?.title ?: "",
          overview = translation?.overview ?: "",
          seasonNumber = season.number,
          episodeNumber = episode.number,
          language = language,
          country = country,
          isLocal = true
        )
      }
    }

    val remoteTranslation = remoteSource.trakt.fetchSeasonTranslations(showId.id, season.number, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslation
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          item.ids.trakt.id,
          showId.id,
          item.title,
          language,
          country,
          item.overview,
          nowUtcMillis()
        )
        localSource.episodesTranslations.insertSingle(dbItem)
      }

    return episodes.map { episode ->
      val translation = findTranslation(episode.ids.trakt.id, remoteTranslation, language, country)
      SeasonTranslation(
        ids = episode.ids.copy(),
        title = translation?.title ?: "",
        overview = translation?.overview ?: "",
        seasonNumber = season.number,
        episodeNumber = episode.number,
        language = language,
        country = country,
        isLocal = true
      )
    }
  }

  private fun findTranslation(translations: List<TraktTranslation>, language: String, country: String): TraktTranslation? {
    var fallback: TraktTranslation? = null
    for (t in translations) {
      if (t.country.equals(country, true)) return t
      if (fallback == null && t.language.equals(language, true)) fallback = t
    }
    return fallback
  }

  private fun findTranslation(episodeId: Long, translations: List<SeasonTranslation>, language: String, country: String): SeasonTranslation? {
    var fallback: SeasonTranslation? = null
    for (t in translations) {
      if (t.ids.trakt.id != episodeId) continue
      if (t.country.equals(country, true)) return t
      if (fallback == null && t.language.equals(language, true)) fallback = t
    }
    return fallback
  }
}
