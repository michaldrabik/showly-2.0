package com.michaldrabik.ui_lists.create.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.ListsRepository
import javax.inject.Inject

@AppScope
class ListDetailsCase @Inject constructor(
  private val listsRepository: ListsRepository,
) {

  suspend fun loadDetails(id: Long) = listsRepository.loadById(id)
}
