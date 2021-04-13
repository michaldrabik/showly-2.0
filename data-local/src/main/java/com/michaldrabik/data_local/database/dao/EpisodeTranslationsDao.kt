package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.EpisodeTranslation

@Dao
interface EpisodeTranslationsDao : BaseDao<EpisodeTranslation> {

  @Query("SELECT * FROM episodes_translations WHERE id_trakt == :traktEpisodeId AND id_trakt_show == :traktShowId AND language == :language")
  suspend fun getById(traktEpisodeId: Long, traktShowId: Long, language: String): EpisodeTranslation?

  @Query("SELECT * FROM episodes_translations WHERE id_trakt IN (:traktEpisodeIds) AND id_trakt_show == :traktShowId AND language == :language")
  suspend fun getByIds(traktEpisodeIds: List<Long>, traktShowId: Long, language: String): List<EpisodeTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(translation: EpisodeTranslation)

  @Query("DELETE FROM episodes_translations WHERE language IN (:languages)")
  suspend fun deleteByLanguage(languages: List<String>)
}
