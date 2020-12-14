package com.michaldrabik.showly2.di.component

import com.michaldrabik.showly2.common.ShowsMoviesSyncService
import com.michaldrabik.showly2.common.TranslationsSyncService
import dagger.Subcomponent

@Subcomponent
interface ServiceComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): ServiceComponent
  }

  fun inject(service: ShowsMoviesSyncService)

  fun inject(service: TranslationsSyncService)
}
