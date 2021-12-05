package com.michaldrabik.ui_people.gallery

import com.michaldrabik.ui_model.Image

data class PersonGalleryUiState(
  val images: List<Image>? = null,
  val isLoading: Boolean = false,
)
