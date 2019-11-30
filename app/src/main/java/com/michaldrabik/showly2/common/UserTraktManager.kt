package com.michaldrabik.showly2.common

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class UserTraktManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

}