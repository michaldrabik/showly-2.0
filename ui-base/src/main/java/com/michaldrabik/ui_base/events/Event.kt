package com.michaldrabik.ui_base.events

sealed class Event

object ShowsSyncComplete : Event()

object TranslationsSyncComplete : Event()

object TraktSyncStart : Event()

object TraktSyncSuccess : Event()

object TraktSyncError : Event()

object TraktSyncAuthError : Event()

data class TraktQuickSyncSuccess(val count: Int) : Event()

data class TraktSyncProgress(val status: String = "") : Event()
