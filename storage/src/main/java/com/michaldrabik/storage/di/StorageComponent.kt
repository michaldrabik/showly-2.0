package com.michaldrabik.storage.di

import dagger.Component

@StorageScope
@Component(modules = [StorageModule::class])
interface StorageComponent : StorageMarker