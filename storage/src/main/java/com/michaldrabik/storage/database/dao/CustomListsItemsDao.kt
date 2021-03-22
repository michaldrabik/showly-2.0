package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.CustomListItem

@Dao
interface CustomListsItemsDao : BaseDao<CustomListItem> {

  @Query("SELECT id_list FROM custom_list_item WHERE id_trakt = :idTrakt AND type = :type")
  suspend fun getListsForItem(idTrakt: Long, type: String): List<Long>

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList AND id_trakt = :idTrakt AND type = :type")
  suspend fun getById(idList: Long, idTrakt: Long, type: String): CustomListItem?

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList ORDER BY rank ASC")
  suspend fun getItemsById(idList: Long): List<CustomListItem>

  @Query("SELECT * FROM custom_list_item WHERE id_list = :idList ORDER BY rank ASC LIMIT :limit")
  suspend fun getItemsForListImages(idList: Long, limit: Int): List<CustomListItem>

  @Query("SELECT rank FROM custom_list_item WHERE id_list = :idList ORDER BY rank DESC LIMIT 1")
  suspend fun getRankForList(idList: Long): Long?

  @Transaction
  suspend fun insertItem(item: CustomListItem) {
    val localItem = getById(item.idList, item.idTrakt, item.type)
    if (localItem != null) return
    val rank = getRankForList(item.idList) ?: 0L
    val rankedItem = item.copy(rank = rank + 1L)
    insert(listOf(rankedItem))
  }

  @Query("DELETE FROM custom_list_item WHERE id_list = :idList AND id_trakt == :idTrakt AND type = :type")
  suspend fun deleteItem(idList: Long, idTrakt: Long, type: String)
}
