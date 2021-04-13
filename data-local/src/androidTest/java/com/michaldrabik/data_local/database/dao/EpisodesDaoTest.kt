@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpisodesDaoTest : BaseDaoTest() {

  @Test
  fun shouldStoreEpisodesForSeason() {
    runBlocking {
      val season = TestData.createSeason()
      val episode1 = TestData.createEpisode().copy(idTrakt = 1)
      val episode2 = TestData.createEpisode().copy(idTrakt = 2)

      database.seasonsDao().upsert(listOf(season))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllForSeason(1)
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))
    }
  }

  @Test
  fun shouldUpdateEpisodeIfAlreadyExists() {
    runBlocking {
      val season = TestData.createSeason()
      val episode1 = TestData.createEpisode().copy(idTrakt = 1)
      val episode2 = TestData.createEpisode().copy(idTrakt = 2)

      database.seasonsDao().upsert(listOf(season))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllForSeason(1)
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))

      val updated = episode2.copy(title = "Updated")
      database.episodesDao().upsert(listOf(episode1, updated))

      val result2 = database.episodesDao().getAllForSeason(1)
      assertThat(result2).containsExactlyElementsIn(listOf(episode1, updated))
    }
  }

  @Test
  fun shouldStoreEpisodesForShows() {
    runBlocking {
      val show1 = TestData.createShow().copy(idTrakt = 1)
      val show2 = TestData.createShow().copy(idTrakt = 2)

      val season1 = TestData.createSeason().copy(idShowTrakt = show1.idTrakt)
      val season2 = TestData.createSeason().copy(idShowTrakt = show2.idTrakt)

      val episode1 = TestData.createEpisode().copy(
        idTrakt = 1,
        idShowTrakt = show1.idTrakt,
        idSeason = season1.idTrakt
      )
      val episode2 = TestData.createEpisode().copy(
        idTrakt = 2,
        idShowTrakt = show2.idTrakt,
        idSeason = season2.idTrakt
      )

      database.showsDao().upsert(listOf(show1, show2))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result2 = database.episodesDao().getAllByShowId(2)
      assertThat(result2).containsExactlyElementsIn(listOf(episode2))
    }
  }

  @Test
  fun shouldReturnWatchedIdsForShow() {
    runBlocking {
      val show = TestData.createShow().copy(idTrakt = 1)

      val season1 = TestData.createSeason().copy(idShowTrakt = show.idTrakt)
      val season2 = TestData.createSeason().copy(idShowTrakt = show.idTrakt)

      val episode1 = TestData.createEpisode().copy(
        idTrakt = 1,
        idShowTrakt = show.idTrakt,
        idSeason = season1.idTrakt,
        isWatched = true
      )
      val episode2 = TestData.createEpisode().copy(
        idTrakt = 2,
        idShowTrakt = show.idTrakt,
        idSeason = season2.idTrakt,
        isWatched = false
      )

      database.showsDao().upsert(listOf(show))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllWatchedIdsForShows(listOf(show.idTrakt))
      assertThat(result).containsExactlyElementsIn(listOf(episode1.idTrakt))
    }
  }

  @Test
  fun shouldDeleteAllUnwatchedForShow() {
    runBlocking {
      val show = TestData.createShow().copy(idTrakt = 1)

      val season1 = TestData.createSeason().copy(idShowTrakt = show.idTrakt)
      val season2 = TestData.createSeason().copy(idShowTrakt = show.idTrakt)

      val episode1 = TestData.createEpisode().copy(
        idTrakt = 1,
        idShowTrakt = show.idTrakt,
        idSeason = season1.idTrakt,
        isWatched = true
      )
      val episode2 = TestData.createEpisode().copy(
        idTrakt = 2,
        idShowTrakt = show.idTrakt,
        idSeason = season2.idTrakt,
        isWatched = false
      )

      database.showsDao().upsert(listOf(show))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllByShowId(show.idTrakt)
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))

      database.episodesDao().deleteAllUnwatchedForShow(show.idTrakt)

      val result2 = database.episodesDao().getAllByShowId(show.idTrakt)
      assertThat(result2).containsExactlyElementsIn(listOf(episode1))
    }
  }
}
