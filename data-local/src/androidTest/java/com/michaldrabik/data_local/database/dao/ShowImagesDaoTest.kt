@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.model.ShowImage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowImagesDaoTest : BaseDaoTest() {

  private val image1 = ShowImage(1, 1, 1, "poster", "show", "url", "thumbUrl", "tmdb")
  private val image2 = ShowImage(2, 2, 2, "poster", "episode", "url", "thumbUrl", "tmdb")
  private val image3 = ShowImage(3, 1, 1, "fanart", "show", "url", "thumbUrl", "tmdb")
  private val image4 = ShowImage(4, 2, 2, "fanart", "episode", "url", "thumbUrl", "tmdb")

  @Before
  fun setUp() = runBlocking {
    database.showImagesDao().upsert(image1)
    database.showImagesDao().upsert(image2)
    database.showImagesDao().upsert(image3)
    database.showImagesDao().upsert(image4)
  }

  @Test
  fun shouldReturnImageByTypeForShow() = runBlocking {
    val result = database.showImagesDao().getByShowId(1, "poster")
    val result2 = database.showImagesDao().getByShowId(1, "fanart")

    assertThat(result).isEqualTo(image1)
    assertThat(result2).isEqualTo(image3)
  }

  @Test
  fun shouldReturnImageByTypeForEpisode() = runBlocking {
    val result = database.showImagesDao().getByEpisodeId(2, "poster")
    val result2 = database.showImagesDao().getByEpisodeId(2, "fanart")

    assertThat(result).isEqualTo(image2)
    assertThat(result2).isEqualTo(image4)
  }

  @Test
  fun shouldUpsertShowImage() = runBlocking {
    val result = database.showImagesDao().getByShowId(10, "fanart")
    assertThat(result).isNull()

    val image = ShowImage(10, 10, 10, "fanart", "show", "url", "thumbUrl", "tmdb")
    database.showImagesDao().insertShowImage(image)

    val result2 = database.showImagesDao().getByShowId(10, "fanart")
    assertThat(result2).isEqualTo(image)

    val image2 = ShowImage(10, 10, 10, "fanart", "show", "url2", "thumbUrl2", "tmdb")
    database.showImagesDao().insertShowImage(image2)

    val result3 = database.showImagesDao().getByShowId(10, "fanart")
    assertThat(result3).isEqualTo(image2)
  }

  @Test
  fun shouldUpsertEpisodeImage() = runBlocking {
    val result = database.showImagesDao().getByEpisodeId(10, "fanart")
    assertThat(result).isNull()

    val image = ShowImage(10, 10, 10, "fanart", "episode", "url", "thumbUrl", "tmdb")
    database.showImagesDao().insertEpisodeImage(image)

    val result2 = database.showImagesDao().getByEpisodeId(10, "fanart")
    assertThat(result2).isEqualTo(image)

    val image2 = ShowImage(10, 10, 10, "fanart", "episode", "url2", "thumbUrl2", "tmdb")
    database.showImagesDao().insertEpisodeImage(image2)

    val result3 = database.showImagesDao().getByEpisodeId(10, "fanart")
    assertThat(result3).isEqualTo(image2)
  }

  @Test
  fun shouldDeleteByShowId() = runBlocking {
    assertThat(database.showImagesDao().getByShowId(1, "poster")).isEqualTo(image1)
    assertThat(database.showImagesDao().getByShowId(1, "fanart")).isEqualTo(image3)

    database.showImagesDao().deleteByShowId(1, "poster")
    database.showImagesDao().deleteByShowId(1, "fanart")

    assertThat(database.showImagesDao().getByShowId(1, "poster")).isNull()
    assertThat(database.showImagesDao().getByShowId(1, "fanart")).isNull()

    // Ensure nothing else is deleted
    assertThat(database.showImagesDao().getByEpisodeId(2, "poster")).isEqualTo(image2)
    assertThat(database.showImagesDao().getByEpisodeId(2, "fanart")).isEqualTo(image4)
  }

  @Test
  fun shouldDeleteByEpisodeId() = runBlocking {
    assertThat(database.showImagesDao().getByEpisodeId(2, "poster")).isEqualTo(image2)
    assertThat(database.showImagesDao().getByEpisodeId(2, "fanart")).isEqualTo(image4)

    database.showImagesDao().deleteByEpisodeId(2, "poster")
    database.showImagesDao().deleteByEpisodeId(2, "fanart")

    assertThat(database.showImagesDao().getByEpisodeId(2, "poster")).isNull()
    assertThat(database.showImagesDao().getByEpisodeId(2, "fanart")).isNull()

    // Ensure nothing else is deleted
    assertThat(database.showImagesDao().getByShowId(1, "poster")).isEqualTo(image1)
    assertThat(database.showImagesDao().getByShowId(1, "fanart")).isEqualTo(image3)
  }
}
