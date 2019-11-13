package com.michaldrabik.showly2.ui.common.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.ui.common.UiModel

open class BaseViewModel<UM : UiModel> : ViewModel() {

  private val _uiStream = MutableLiveData<UM>()
  val uiStream get() = _uiStream

  @Suppress("UNCHECKED_CAST")
  protected var uiState: UM?
    get() = _uiStream.value
    set(value) = when (value) {
      null -> _uiStream.value = value
      else -> _uiStream.value = _uiStream.value?.update(value) as? UM ?: value
    }

  override fun onCleared() {
    super.onCleared()
    Log.i("BaseViewModel", "ViewModel cleared.")
  }
}