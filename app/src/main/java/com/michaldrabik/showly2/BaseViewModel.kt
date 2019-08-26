package com.michaldrabik.showly2

import android.util.Log
import androidx.lifecycle.ViewModel

open class BaseViewModel(
) : ViewModel() {

  override fun onCleared() {
    super.onCleared()
    Log.i("BaseViewModel", "ViewModel cleared.")
  }
}