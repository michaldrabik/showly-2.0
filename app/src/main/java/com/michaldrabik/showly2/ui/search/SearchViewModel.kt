package com.michaldrabik.showly2.ui.search

import androidx.lifecycle.MutableLiveData
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject

class SearchViewModel @Inject constructor(
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<SearchUiModel>() }

}
