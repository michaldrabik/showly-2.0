package com.michaldrabik.showly2.ui.common.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.ui.common.UiModel

open class BaseViewModel<UM : UiModel> : ViewModel() {

  protected val _uiStream = MutableLiveData<UM>()
  val uiStream: LiveData<UM> = _uiStream

  override fun onCleared() {
    super.onCleared()
    Log.i("BaseViewModel", "ViewModel cleared.")
  }
}