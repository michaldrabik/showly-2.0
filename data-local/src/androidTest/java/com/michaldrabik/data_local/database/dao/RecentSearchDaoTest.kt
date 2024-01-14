@file:Suppress("DEPRECATION")

package com.michaldrabik.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.model.RecentSearch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentSearchDaoTest : BaseDaoTest() {

  private val items = mutableListOf<RecentSearch>()

  @Before
  fun setUp() {
    val item = RecentSearch(1, "Text1", 1, 1)
    val item2 = RecentSearch(2, "Text2", 2, 2)
    val item3 = RecentSearch(3, "Text3", 3, 3)
    items.addAll(listOf(item, item2, item3))
  }

  @Test
  fun shouldInsertAndStoreEntities() {
    runBlocking {
      database.recentSearchDao().upsert(items)

      val result = database.recentSearchDao().getAll(10)
      assertThat(result).containsExactlyElementsIn(items)
    }
  }

  @Test
  fun shouldReturnMostRecentSearchesFirst() {
    runBlocking {
      database.recentSearchDao().upsert(items)

      val result = database.recentSearchDao().getAll(10)
      assertThat(result).containsExactlyElementsIn(items)
      assertThat(result).isInOrder(compareByDescending<RecentSearch> { it.createdAt })
    }
  }

  @Test
  fun shouldReturnLimitedResults() {
    runBlocking {
      database.recentSearchDao().upsert(items)

      val result = database.recentSearchDao().getAll(2)
      assertThat(result).hasSize(2)
    }
  }
}
