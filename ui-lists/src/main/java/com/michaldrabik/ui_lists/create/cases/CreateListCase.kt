package com.michaldrabik.ui_lists.create.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_repository.ListsRepository
import javax.inject.Inject

@AppScope
class CreateListCase @Inject constructor(
  private val listsRepository: ListsRepository,
) {

  suspend fun createList(name: String, description: String?) = listsRepository.createList(name, description)
}
