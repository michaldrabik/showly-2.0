package com.michaldrabik.ui_people.gallery

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
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
) : BaseViewModel() {

  private val imagesState = MutableStateFlow<List<Image>?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadImages(id: IdTmdb) {
    viewModelScope.launch {
      val initialImage = imagesCase.loadInitialImage(id)
      if (initialImage?.status == ImageStatus.AVAILABLE) {
        imagesState.value = listOf(initialImage)
      }
      try {
        loadingState.value = true
        val allImages = imagesCase.loadAllImages(id, initialImage)
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
