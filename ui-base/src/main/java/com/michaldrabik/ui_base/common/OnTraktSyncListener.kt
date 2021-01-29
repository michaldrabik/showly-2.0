package com.michaldrabik.ui_base.common

interface OnTraktSyncListener {
  fun onTraktSyncComplete()
  fun onTraktSyncProgress() = Unit
  fun isTraktSyncActive() = false
}
