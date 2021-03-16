package com.michaldrabik.ui_lists.manage

import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_lists.manage.cases.ManageListsCase
import javax.inject.Inject

class ManageListsViewModel @Inject constructor(
  private val manageListsCase: ManageListsCase
) : BaseViewModel<ManageListsUiModel>() {

}
