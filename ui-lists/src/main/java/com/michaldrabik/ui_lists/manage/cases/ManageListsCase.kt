package com.michaldrabik.ui_lists.manage.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_repository.ListsRepository
import javax.inject.Inject

@AppScope
class ManageListsCase @Inject constructor(
  private val listsRepository: ListsRepository,
) {

  suspend fun createList(name: String, description: String?) =
    listsRepository.createList(name, description)

  suspend fun updateList(list: CustomList, name: String, description: String?) =
    listsRepository.updateList(list.id, name, description)
}
