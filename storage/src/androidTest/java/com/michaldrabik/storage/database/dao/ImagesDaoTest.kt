@file:Suppress("DEPRECATION")

package com.michaldrabik.storage.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.storage.database.model.Image
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagesDaoTest : BaseDaoTest() {

  private val image1 = Image(1, 1, "poster", "show", "url", "thumbUrl")
  private val image2 = Image(2, 2, "poster", "episode", "url", "thumbUrl")
  private val image3 = Image(3, 1, "fanart", "show", "url", "thumbUrl")
  private val image4 = Image(4, 2, "fanart", "episode", "url", "thumbUrl")

  @Before
  fun setUp() = runBlocking {
    database.imagesDao().upsert(image1)
    database.imagesDao().upsert(image2)
    database.imagesDao().upsert(image3)
    database.imagesDao().upsert(image4)
  }

  @Test
  fun shouldReturnImageByTypeForShow() = runBlocking {
    val result = database.imagesDao().getByShowId(1, "poster")
    val result2 = database.imagesDao().getByShowId(1, "fanart")

    assertThat(result).isEqualTo(image1)
    assertThat(result2).isEqualTo(image3)
  }

  @Test
  fun shouldReturnImageByTypeForEpisode() = runBlocking {
    val result = database.imagesDao().getByEpisodeId(2, "poster")
    val result2 = database.imagesDao().getByEpisodeId(2, "fanart")

    assertThat(result).isEqualTo(image2)
    assertThat(result2).isEqualTo(image4)
  }

  @Test
  fun shouldUpsertShowImage() = runBlocking {
    val result = database.imagesDao().getByShowId(10, "fanart")
    assertThat(result).isNull()

    val image = Image(10, 10, "fanart", "show", "url", "thumbUrl")
    database.imagesDao().insertShowImage(image)

    val result2 = database.imagesDao().getByShowId(10, "fanart")
    assertThat(result2).isEqualTo(image)

    val image2 = Image(10, 10, "fanart", "show", "url2", "thumbUrl2")
    database.imagesDao().insertShowImage(image2)

    val result3 = database.imagesDao().getByShowId(10, "fanart")
    assertThat(result3).isEqualTo(image2)
  }

  @Test
  fun shouldUpsertEpisodeImage() = runBlocking {
    val result = database.imagesDao().getByEpisodeId(10, "fanart")
    assertThat(result).isNull()

    val image = Image(10, 10, "fanart", "episode", "url", "thumbUrl")
    database.imagesDao().insertEpisodeImage(image)

    val result2 = database.imagesDao().getByEpisodeId(10, "fanart")
    assertThat(result2).isEqualTo(image)

    val image2 = Image(10, 10, "fanart", "episode", "url2", "thumbUrl2")
    database.imagesDao().insertEpisodeImage(image2)

    val result3 = database.imagesDao().getByEpisodeId(10, "fanart")
    assertThat(result3).isEqualTo(image2)
  }

  @Test
  fun shouldDeleteByShowId() = runBlocking {
    assertThat(database.imagesDao().getByShowId(1, "poster")).isEqualTo(image1)
    assertThat(database.imagesDao().getByShowId(1, "fanart")).isEqualTo(image3)

    database.imagesDao().deleteByShowId(1, "poster")
    database.imagesDao().deleteByShowId(1, "fanart")

    assertThat(database.imagesDao().getByShowId(1, "poster")).isNull()
    assertThat(database.imagesDao().getByShowId(1, "fanart")).isNull()

    // Ensure nothing else is deleted
    assertThat(database.imagesDao().getByEpisodeId(2, "poster")).isEqualTo(image2)
    assertThat(database.imagesDao().getByEpisodeId(2, "fanart")).isEqualTo(image4)
  }

  @Test
  fun shouldDeleteByEpisodeId() = runBlocking {
    assertThat(database.imagesDao().getByEpisodeId(2, "poster")).isEqualTo(image2)
    assertThat(database.imagesDao().getByEpisodeId(2, "fanart")).isEqualTo(image4)

    database.imagesDao().deleteByEpisodeId(2, "poster")
    database.imagesDao().deleteByEpisodeId(2, "fanart")

    assertThat(database.imagesDao().getByEpisodeId(2, "poster")).isNull()
    assertThat(database.imagesDao().getByEpisodeId(2, "fanart")).isNull()

    // Ensure nothing else is deleted
    assertThat(database.imagesDao().getByShowId(1, "poster")).isEqualTo(image1)
    assertThat(database.imagesDao().getByShowId(1, "fanart")).isEqualTo(image3)
  }
}
