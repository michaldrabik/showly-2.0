package com.michaldrabik.ui_lists.my_lists.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import javax.inject.Inject

@AppScope
class MainMyListsCase @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

}
