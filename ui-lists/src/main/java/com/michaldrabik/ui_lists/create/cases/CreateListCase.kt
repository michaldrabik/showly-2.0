package com.michaldrabik.ui_lists.create.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError.ResourceNotFoundError
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktListQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_model.CustomList
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.delay
import javax.inject.Inject

@ViewModelScoped
class CreateListCase @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val settingsRepository: SettingsRepository,
  private val userTraktManager: UserTraktManager,
  private val eventsManager: EventsManager,
) {

  suspend fun createList(name: String, description: String?): CustomList {
    val isAuthorized = userTraktManager.isAuthorized()
    val isQuickSyncEnabled = settingsRepository.load().traktQuickSyncEnabled
    if (isAuthorized && isQuickSyncEnabled) {
      userTraktManager.checkAuthorization()
      val list = remoteSource.trakt.postCreateList(name, description).run {
        mappers.customList.fromNetwork(this)
      }
      return listsRepository.createList(name, description, list.idTrakt, list.idSlug)
        .also { eventsManager.sendEvent(TraktListQuickSyncSuccess) }
    }
    return listsRepository.createList(name, description, null, null)
  }

  suspend fun updateList(list: CustomList): CustomList {
    val isAuthorized = userTraktManager.isAuthorized()
    val isQuickSyncEnabled = settingsRepository.load().traktQuickSyncEnabled

    if (isAuthorized && isQuickSyncEnabled) {
      userTraktManager.checkAuthorization()
      val updateList = mappers.customList.toNetwork(list)
      return try {
        val result = remoteSource.trakt.postUpdateList(updateList).run {
          mappers.customList.fromNetwork(this)
        }
        listsRepository.updateList(list.id, result.idTrakt, result.idSlug, result.name, result.description)
          .also { eventsManager.sendEvent(TraktQuickSyncSuccess(1)) }
      } catch (error: Throwable) {
        if (ErrorHelper.parse(error) is ResourceNotFoundError) {
          // If list does not exist in Trakt account we need to create it and upload items as well.
          delay(1000)
          val result = remoteSource.trakt.postCreateList(updateList.name, updateList.description)
            .run { mappers.customList.fromNetwork(this) }
          listsRepository.updateList(list.id, result.idTrakt, result.idSlug, result.name, result.description)

          val localItems = listsRepository.loadListItemsForId(list.id)
          if (localItems.isNotEmpty()) {
            val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
            val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }
            delay(1000)
            remoteSource.trakt.postAddListItems(result.idTrakt!!, showsIds, moviesIds)
          }

          listsRepository.updateList(list.id, result.idTrakt, result.idSlug, result.name, result.description)
            .also { eventsManager.sendEvent(TraktQuickSyncSuccess(1)) }
        } else {
          Logger.record(error, "CreateListCase::updateList()")
          throw error
        }
      }
    }

    return listsRepository.updateList(list.id, null, null, list.name, list.description)
  }
}
