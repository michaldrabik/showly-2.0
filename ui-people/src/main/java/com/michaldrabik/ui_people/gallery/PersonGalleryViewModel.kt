package com.michaldrabik.ui_people.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.gallery.cases.PersonGalleryImagesCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonGalleryViewModel @Inject constructor(
  private val imagesCase: PersonGalleryImagesCase,
) : ViewModel() {

  private val imagesState = MutableStateFlow<List<Image>?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadImages(id: IdTmdb) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        val allImages = imagesCase.loadImages(id)
        imagesState.value = allImages
        loadingState.value = false
      } catch (t: Throwable) {
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    imagesState,
    loadingState
  ) { s1, s2 ->
    PersonGalleryUiState(
      images = s1,
      isLoading = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = PersonGalleryUiState()
  )
}
