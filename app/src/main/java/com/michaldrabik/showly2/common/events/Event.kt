package com.michaldrabik.showly2.common.events

sealed class Event

object ShowsSyncComplete : Event()

// Trakt Import

object TraktImportStart : Event()

object TraktImportSuccess : Event()

object TraktImportError : Event()

object TraktImportAuthError : Event()

object TraktImportProgress : Event()

// Trakt Export

object TraktExportStart : Event()

object TraktExportSuccess : Event()

object TraktExportError : Event()

object TraktExportProgress : Event()
