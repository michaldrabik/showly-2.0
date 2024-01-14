package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.data_local.sources.CustomListsItemsLocalDataSource

@Dao
interface CustomListsItemsDao : CustomListsItemsLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(items: List<CustomListItem>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun update(items: List<CustomListItem>)

  @Query("SELECT id_list FROM custom_list_item WHERE id_trakt = :idTrakt AND type = :type")
  override suspend fun getListsForItem(idTrakt: Long, type: String): List<Long>

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList AND id_trakt = :idTrakt AND type = :type")
  override suspend fun getByIdTrakt(idList: Long, idTrakt: Long, type: String): CustomListItem?

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList ORDER BY rank ASC")
  override suspend fun getItemsById(idList: Long): List<CustomListItem>

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList ORDER BY rank ASC LIMIT :limit")
  override suspend fun getItemsForListImages(idList: Long, limit: Int): List<CustomListItem>

  @Query("SELECT rank FROM custom_list_item WHERE id_list = :idList ORDER BY rank DESC LIMIT 1")
  override suspend fun getRankForList(idList: Long): Long?

  @Transaction
  override suspend fun insertItem(item: CustomListItem) {
    val localItem = getByIdTrakt(item.idList, item.idTrakt, item.type)
    if (localItem != null) return
    val rank = getRankForList(item.idList) ?: 0L
    val rankedItem = item.copy(rank = rank + 1L)
    insert(listOf(rankedItem))
  }

  @Query("DELETE FROM custom_list_item WHERE id_list = :idList AND id_trakt == :idTrakt AND type = :type")
  override suspend fun deleteItem(idList: Long, idTrakt: Long, type: String)
}
