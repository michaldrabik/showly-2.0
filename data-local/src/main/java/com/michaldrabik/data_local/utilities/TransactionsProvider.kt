package com.michaldrabik.data_local.utilities

import androidx.room.RoomDatabase
import androidx.room.withTransaction

class TransactionsProvider(
  private val database: RoomDatabase
) {
  suspend fun <R> withTransaction(block: suspend () -> R): R {
    return database.withTransaction(block)
  }
}
