package com.michaldrabik.data_local.di

import dagger.Component

@StorageScope
@Component(modules = [StorageModule::class])
interface StorageComponent : StorageContract
