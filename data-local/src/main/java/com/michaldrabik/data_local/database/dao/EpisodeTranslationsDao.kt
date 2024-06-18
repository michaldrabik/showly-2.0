package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.EpisodeTranslation
import com.michaldrabik.data_local.sources.EpisodeTranslationsLocalDataSource

@Dao
interface EpisodeTranslationsDao : BaseDao<EpisodeTranslation>, EpisodeTranslationsLocalDataSource {

  @Query("SELECT * FROM episodes_translations WHERE id_trakt == :traktEpisodeId AND id_trakt_show == :traktShowId AND language == :language AND country == :country")
  override suspend fun getById(traktEpisodeId: Long, traktShowId: Long, language: String, country: String): EpisodeTranslation?

  @Query("SELECT * FROM episodes_translations WHERE id_trakt IN (:traktEpisodeIds) AND id_trakt_show == :traktShowId AND language == :language AND country == :country")
  override suspend fun getByIds(traktEpisodeIds: List<Long>, traktShowId: Long, language: String, country: String): List<EpisodeTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insertSingle(translation: EpisodeTranslation)

  @Query("DELETE FROM episodes_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)

  @Query("DELETE FROM episodes_translations WHERE country IN (:countries)")
  override suspend fun deleteByCountry(countries: List<String>)
}
