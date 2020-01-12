package com.michaldrabik.showly2.ui.common.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.utilities.SingleLiveEvent

@Suppress("PropertyName")
open class BaseViewModel<UM : UiModel> : ViewModel() {

  private val _uiLiveData = MutableLiveData<UM>()
  val uiLiveData: LiveData<UM> get() = _uiLiveData

  protected val _messageLiveData = SingleLiveEvent<Int>()
  val messageLiveData: LiveData<Int> get() = _messageLiveData

  protected val _errorLiveData = SingleLiveEvent<Int>()
  val errorLiveData: LiveData<Int> get() = _errorLiveData

  @Suppress("UNCHECKED_CAST")
  protected var uiState: UM?
    get() = _uiLiveData.value
    set(value) = when (value) {
      null -> _uiLiveData.value = value
      else -> _uiLiveData.value = _uiLiveData.value?.update(value) as? UM ?: value
    }

  override fun onCleared() {
    super.onCleared()
    Log.i("BaseViewModel", "ViewModel cleared.")
  }
}
