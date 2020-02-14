package com.michaldrabik.showly2.common.events

sealed class Event

object ShowsSyncComplete : Event()

object TraktSyncStart : Event()

object TraktSyncSuccess : Event()

object TraktSyncError : Event()

object TraktSyncAuthError : Event()

object TraktSyncProgress : Event()
