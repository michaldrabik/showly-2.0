package com.michaldrabik.ui_base.events

sealed class Event

data class ShowsMoviesSyncComplete(val count: Int) : Event()

object TraktSyncStart : Event()

object TraktSyncSuccess : Event()

object TraktSyncError : Event()

object TraktSyncAuthError : Event()

data class TraktQuickSyncSuccess(val count: Int) : Event()

object TraktListQuickSyncSuccess : Event()

data class TraktSyncProgress(val status: String = "") : Event()
