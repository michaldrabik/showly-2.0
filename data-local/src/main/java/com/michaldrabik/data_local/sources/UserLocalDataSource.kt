package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.User

interface UserLocalDataSource {

  suspend fun get(): User?

  suspend fun upsert(user: User)
}
