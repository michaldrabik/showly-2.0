package com.michaldrabik.showly2.ui.shows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.storage.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val interactor: ShowDetailsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<ShowDetailsUiModel>() }

  fun loadShowDetails(id: Long) {
    viewModelScope.launch {
      val show = interactor.loadShowDetails(id)
      uiStream.value = ShowDetailsUiModel(show)
      val image = interactor.loadBackgroundImage(show)
      if (image != null) {
        uiStream.value = ShowDetailsUiModel(image = image)
      }
    }
  }
}
