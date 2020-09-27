package com.michaldrabik.ui_base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.utilities.MessageEvent
import timber.log.Timber

@Suppress("PropertyName")
open class BaseViewModel<UM : UiModel> : ViewModel() {

  private val _uiLiveData = MutableLiveData<UM>()
  val uiLiveData: LiveData<UM> get() = _uiLiveData

  protected val _messageLiveData = MutableLiveData<MessageEvent>()
  val messageLiveData: LiveData<MessageEvent> get() = _messageLiveData

  @Suppress("UNCHECKED_CAST")
  protected var uiState: UM?
    get() = _uiLiveData.value
    set(value) = when (value) {
      null -> _uiLiveData.value = value
      else -> _uiLiveData.value = _uiLiveData.value?.update(value) as? UM ?: value
    }

  override fun onCleared() {
    super.onCleared()
    Timber.d("ViewModel cleared.")
  }
}
