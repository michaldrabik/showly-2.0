package com.michaldrabik.ui_lists

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_lists.cases.MainMyListsCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyListsViewModel @Inject constructor(
  private val mainCase: MainMyListsCase
) : BaseViewModel<MyListsUiModel>() {

  var searchViewTranslation = 0F
  var tabsTranslation = 0F

  fun loadItems() {
    viewModelScope.launch {
      uiState = MyListsUiModel(items = emptyList())
    }
  }
}
