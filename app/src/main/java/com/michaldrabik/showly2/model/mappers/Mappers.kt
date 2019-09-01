package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Image as ImageDb

@AppScope
class Mappers @Inject constructor(
  val image: ImageMapper,
  val show: ShowMapper
)