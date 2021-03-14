package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class MainListDetailsCase @Inject constructor(
  private val listsRepository: ListsRepository,
  private val dateProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadDetails(id: Long) = listsRepository.loadById(id)

  suspend fun deleteList(listId: Long) = listsRepository.deleteList(listId)
}
