package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val interactor: ShowDetailsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  fun loadShowDetails(id: Long) {
    viewModelScope.launch {
      try {
        val show = interactor.loadShowDetails(id)
        uiStream.value = ShowDetailsUiModel(show)

        uiStream.value = ShowDetailsUiModel(imageLoading = true)
        val image = interactor.loadBackgroundImage(show)
        uiStream.value = ShowDetailsUiModel(image = image)
      } catch (error: Throwable) {
        //TODO
        uiStream.value = ShowDetailsUiModel(imageLoading = false)
      }
    }
  }
}
