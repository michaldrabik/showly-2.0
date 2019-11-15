package com.michaldrabik.showly2.ui.common.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.utilities.SingleLiveEvent

open class BaseViewModel<UM : UiModel> : ViewModel() {

  private val _uiStream = MutableLiveData<UM>()
  val uiStream: LiveData<UM> get() = _uiStream

  protected val _messageStream = SingleLiveEvent<Int>()
  val messageStream: LiveData<Int> get() = _messageStream

  protected val _errorStream = SingleLiveEvent<Int>()
  val errorStream: LiveData<Int> get() = _errorStream

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