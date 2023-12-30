// Copyright (C) 2023, Jamie Sanson
// SPDX-License-Identifier: Apache-2.0
package dev.sanson.lightroom.sdk.backend.auth

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dev.sanson.lightroom.core.data.JsonSerializer
import dev.sanson.lightroom.sdk.backend.auth.api.LightroomAuthService
import dev.sanson.lightroom.sdk.di.FilesDir
import dev.sanson.lightroom.sdk.di.VerboseLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
internal annotation class LoginHost

private const val ADOBE_LOGIN_HOST = "https://ims-na1.adobelogin.com"

@Module
internal class AuthModule {
    @Provides
    @Singleton
    fun provideCredentialStore(dataStore: DataStore<Credential?>): CredentialStore {
        return DefaultCredentialStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideCredentialDataStore(
        scope: CoroutineScope,
        @FilesDir filesDir: File,
    ): DataStore<Credential?> {
        return DataStoreFactory.create(
            serializer = JsonSerializer<Credential>(),
            scope = scope,
            produceFile = {
                File("${filesDir.path}/credentials")
            },
        )
    }

    @Provides
    @LoginHost
    fun provideLoginHost(): String = ADOBE_LOGIN_HOST

    @Provides
    fun provideAuthService(
        @LoginHost loginHost: String,
        @VerboseLogging verboseLogging: Boolean,
        json: Json,
    ): LightroomAuthService {
        return Retrofit.Builder().client(
            OkHttpClient.Builder().addInterceptor(
                HttpLoggingInterceptor().apply {
                    level =
                        if (verboseLogging) {
                            HttpLoggingInterceptor.Level.BODY
                        } else {
                            HttpLoggingInterceptor.Level.NONE
                        }
                },
            ).build(),
        ).baseUrl(loginHost)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create<LightroomAuthService>()
    }
}
