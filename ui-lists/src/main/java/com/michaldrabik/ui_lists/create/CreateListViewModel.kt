package com.michaldrabik.ui_lists.create

import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_repository.CommentsRepository
import javax.inject.Inject

class CreateListViewModel @Inject constructor(
  private val commentsRepository: CommentsRepository
) : BaseViewModel<CreateListUiModel>() {

}
