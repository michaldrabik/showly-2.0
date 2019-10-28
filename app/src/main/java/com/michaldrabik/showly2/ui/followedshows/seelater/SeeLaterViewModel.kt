package com.michaldrabik.showly2.ui.followedshows.seelater

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class SeeLaterViewModel @Inject constructor(
  private val interactor: SeeLaterInteractor
) : BaseViewModel() {

  private val _uiStream = MutableLiveData<SeeLaterUiModel>()
  val uiStream: LiveData<SeeLaterUiModel> = _uiStream

  fun loadShows() {
    viewModelScope.launch {
      val items = interactor.loadShows().map {
        val image = interactor.findCachedImage(it, POSTER)
        SeeLaterListItem(it, image, false)
      }
      _uiStream.value = SeeLaterUiModel(items = items)
    }
  }

  fun loadMissingImage(item: SeeLaterListItem, force: Boolean) =
    viewModelScope.launch {
      _uiStream.value = SeeLaterUiModel(updateItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        _uiStream.value =
          SeeLaterUiModel(updateItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        _uiStream.value =
          SeeLaterUiModel(updateItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
}
