package com.michaldrabik.showly2.di.component

import com.michaldrabik.showly2.common.ShowsSyncService
import com.michaldrabik.showly2.common.trakt.exports.TraktExportService
import com.michaldrabik.showly2.common.trakt.imports.TraktImportService
import dagger.Subcomponent

@Subcomponent
interface ServiceComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): ServiceComponent
  }

  fun inject(service: ShowsSyncService)

  fun inject(service: TraktImportService)

  fun inject(service: TraktExportService)
}
