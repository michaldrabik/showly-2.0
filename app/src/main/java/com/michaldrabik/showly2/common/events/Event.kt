package com.michaldrabik.showly2.common.events

sealed class Event

object ShowsSyncComplete : Event()

object TraktImportStart : Event()

object TraktImportSuccess : Event()

object TraktImportError : Event()

object TraktImportAuthError : Event()

object TraktImportProgress : Event()
