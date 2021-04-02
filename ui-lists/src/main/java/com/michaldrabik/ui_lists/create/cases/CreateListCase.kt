package com.michaldrabik.ui_lists.create.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.mappers.Mappers
import retrofit2.HttpException
import javax.inject.Inject

@AppScope
class CreateListCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val settingsRepository: SettingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun createList(name: String, description: String?): CustomList {
    val isAuthorized = userTraktManager.isAuthorized()
    val isQuickSyncEnabled = settingsRepository.load().traktQuickSyncEnabled
    if (isAuthorized && isQuickSyncEnabled) {
      val token = userTraktManager.checkAuthorization()
      val list = cloud.traktApi.postCreateList(token.token, name, description).run {
        mappers.customList.fromNetwork(this)
      }
      return listsRepository.createList(name, description, list.idTrakt, list.idSlug)
        .also { EventsManager.sendEvent(TraktQuickSyncSuccess(1)) }
    }
    return listsRepository.createList(name, description, null, null)
  }

  suspend fun updateList(list: CustomList): CustomList {
    val isAuthorized = userTraktManager.isAuthorized()
    val isQuickSyncEnabled = settingsRepository.load().traktQuickSyncEnabled

    if (isAuthorized && isQuickSyncEnabled) {
      val token = userTraktManager.checkAuthorization()
      val updateList = mappers.customList.toNetwork(list)
      return try {
        val result = cloud.traktApi.postUpdateList(token.token, updateList).run {
          mappers.customList.fromNetwork(this)
        }
        listsRepository.updateList(list.id, result.name, result.description)
          .also { EventsManager.sendEvent(TraktQuickSyncSuccess(1)) }
      } catch (error: Throwable) {
        if (error is HttpException && error.code() == 404) {
          // If list does not exist in Trakt account simply update it locally. It will be created/synced during Trakt Sync.
          listsRepository.updateList(list.id, list.name, list.description)
        } else {
          throw error
        }
      }
    }

    return listsRepository.updateList(list.id, list.name, list.description)
  }
}
