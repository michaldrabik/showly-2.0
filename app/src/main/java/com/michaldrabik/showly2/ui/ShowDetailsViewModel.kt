package com.michaldrabik.showly2.ui

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.storage.cache.ImagesUrlCache
import com.michaldrabik.storage.repository.UserRepository
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
  private val cloud: Cloud,
  private val userRepository: UserRepository,
  private val imagesCache: ImagesUrlCache
) : BaseViewModel() {

}