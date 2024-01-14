package com.michaldrabik.ui_base.events

sealed class Event

object ReloadData : Event()

// Trakt Sync

object TraktSyncStart : Event()

object TraktSyncSuccess : Event()

object TraktSyncError : Event()

object TraktSyncAuthError : Event()

data class TraktSyncProgress(val status: String = "") : Event()

// Trakt Instant Sync

data class TraktQuickSyncSuccess(val count: Int) : Event()

object TraktListQuickSyncSuccess : Event()

// Shows, Movies Sync

data class ShowsMoviesSyncComplete(val count: Int) : Event()
