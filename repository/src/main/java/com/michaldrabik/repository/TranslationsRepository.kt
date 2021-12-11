package com.michaldrabik.repository

import android.content.SharedPreferences
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.EpisodeTranslation
import com.michaldrabik.data_local.database.model.MovieTranslation
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.data_local.database.model.TranslationsMoviesSyncLog
import com.michaldrabik.data_local.database.model.TranslationsSyncLog
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.SettingsRepository.Key.LANGUAGE
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TranslationsRepository @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  fun getLanguage() = miscPreferences.getString(LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

  suspend fun loadAllShowsLocal(language: String = DEFAULT_LANGUAGE): Map<Long, Translation> {
    val local = database.showTranslationsDao().getAll(language)
    return local.associate {
      Pair(it.idTrakt, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadAllMoviesLocal(language: String = DEFAULT_LANGUAGE): Map<Long, Translation> {
    val local = database.movieTranslationsDao().getAll(language)
    return local.associate {
      Pair(it.idTrakt, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadTranslation(
    show: Show,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val local = database.showTranslationsDao().getById(show.traktId, language)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = database.translationsSyncLogDao().getById(show.traktId)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      cloud.traktApi.fetchShowTranslations(show.traktId, language).firstOrNull()
    } catch (error: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = ShowTranslation.fromTraktId(
      show.traktId,
      translation.title,
      language,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      database.showTranslationsDao().insert(translationDb)
    }
    database.translationsSyncLogDao().upsert(TranslationsSyncLog(show.traktId, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    movie: Movie,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val local = database.movieTranslationsDao().getById(movie.traktId, language)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = database.translationsMoviesSyncLogDao().getById(movie.traktId)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      cloud.traktApi.fetchMovieTranslations(movie.traktId, language).firstOrNull()
    } catch (error: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = MovieTranslation.fromTraktId(
      movie.traktId,
      translation.title,
      language,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      database.movieTranslationsDao().insert(translationDb)
    }
    database.translationsMoviesSyncLogDao().upsert(TranslationsMoviesSyncLog(movie.traktId, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    episode: Episode,
    showId: IdTrakt,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val nowMillis = nowUtcMillis()
    val local = database.episodeTranslationsDao().getById(episode.ids.trakt.id, showId.id, language)
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

    val remoteTranslations = cloud.traktApi.fetchSeasonTranslations(showId.id, episode.season, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslations
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          traktEpisodeId = item.ids.trakt.id,
          traktShowId = showId.id,
          title = item.title,
          overview = item.overview,
          language = language,
          createdAt = nowMillis
        )
        database.episodeTranslationsDao().insert(dbItem)
      }

    remoteTranslations
      .find { it.ids.trakt == episode.ids.trakt }
      ?.let {
        return Translation(it.title, it.overview, it.language)
      }

    return null
  }

  suspend fun loadTranslations(
    season: Season,
    showId: IdTrakt,
    language: String = DEFAULT_LANGUAGE
  ): List<SeasonTranslation> {
    val episodes = season.episodes.toList()
    val episodesIds = season.episodes.map { it.ids.trakt.id }

    val local = database.episodeTranslationsDao().getByIds(episodesIds, showId.id, language)
    val hasAllTranslated = local.isNotEmpty() && local.all { it.title.isNotBlank() && it.overview.isNotBlank() }
    val isCacheValid = local.isNotEmpty() && nowUtcMillis() - local.first().updatedAt < ConfigVariant.TRANSLATION_SYNC_EPISODE_COOLDOWN

    if (hasAllTranslated || (!hasAllTranslated && isCacheValid)) {
      return episodes.map { episode ->
        val translation = local.find { it.idTrakt == episode.ids.trakt.id }
        SeasonTranslation(
          ids = episode.ids.copy(),
          title = translation?.title ?: "",
          overview = translation?.overview ?: "",
          seasonNumber = season.number,
          episodeNumber = episode.number,
          language = language,
          isLocal = true
        )
      }
    }

    val remoteTranslation = cloud.traktApi.fetchSeasonTranslations(showId.id, season.number, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslation
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          item.ids.trakt.id,
          showId.id,
          item.title,
          language,
          item.overview,
          nowUtcMillis()
        )
        database.episodeTranslationsDao().insert(dbItem)
      }

    return episodes.map { episode ->
      val translation = remoteTranslation.find { it.ids.trakt.id == episode.ids.trakt.id }
      SeasonTranslation(
        ids = episode.ids.copy(),
        title = translation?.title ?: "",
        overview = translation?.overview ?: "",
        seasonNumber = season.number,
        episodeNumber = episode.number,
        language = language,
        isLocal = true
      )
    }
  }
}
