// file: di/RemoteBindings.kt
package com.example.fernfreunde.di

import com.example.fernfreunde.data.remote.*
import com.example.fernfreunde.data.remote.dataSources.FirestoreFriendshipDataSource
import com.example.fernfreunde.data.remote.dataSources.FirestoreUserDataSource
import com.example.fernfreunde.data.remote.dataSources.IFirestoreFriendshipDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteBindings {

    @Binds
    @Singleton
    abstract fun bindFriendshipRemote(impl: FirestoreFriendshipDataSource): IFirestoreFriendshipDataSource
}
