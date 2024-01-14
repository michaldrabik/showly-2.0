package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.CustomListItem

interface CustomListsItemsLocalDataSource {

  suspend fun update(items: List<CustomListItem>)

  suspend fun getListsForItem(idTrakt: Long, type: String): List<Long>

  suspend fun getByIdTrakt(idList: Long, idTrakt: Long, type: String): CustomListItem?

  suspend fun getItemsById(idList: Long): List<CustomListItem>

  suspend fun getItemsForListImages(idList: Long, limit: Int): List<CustomListItem>

  suspend fun getRankForList(idList: Long): Long?

  suspend fun insertItem(item: CustomListItem)

  suspend fun deleteItem(idList: Long, idTrakt: Long, type: String)
}
