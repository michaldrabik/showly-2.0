package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.CustomListItems

@Dao
interface CustomListsItemsDao : BaseDao<CustomListItems> {

  @Query("SELECT * FROM custom_lists_items WHERE id_trakt = :idTrakt AND type = :type")
  suspend fun getById(idTrakt: Long, type: String): CustomListItems?

  @Transaction
  suspend fun insertItem(item: CustomListItems) {
    val localItem = getById(item.idTrakt, item.type)
    if (localItem != null) return
    insert(listOf(item))
  }

  @Query("DELETE FROM custom_lists_items WHERE id_trakt == :idTrakt AND type = :type")
  suspend fun deleteItem(idTrakt: Long, type: String)
}
