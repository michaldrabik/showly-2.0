package com.michaldrabik.ui_lists.create.cases

import com.michaldrabik.repository.ListsRepository
import javax.inject.Inject

class ListDetailsCase @Inject constructor(
  private val listsRepository: ListsRepository,
) {

  suspend fun loadDetails(id: Long) = listsRepository.loadById(id)
}
