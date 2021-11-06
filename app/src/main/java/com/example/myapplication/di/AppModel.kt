package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.data.network.ApiJobService
import com.example.myapplication.qualifiers.IOThread
import com.example.myapplication.qualifiers.MainThread
import com.example.myapplication.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import myappnew.com.conserve.data.JobDataBase
import retrofit2.Retrofit
import java.util.*
import javax.inject.Singleton
import okhttp3.Request;
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor





@Module
@InstallIn(SingletonComponent::class)
object AppModel {


    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ) = context

    @MainThread
    @Singleton
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @IOThread
    @Singleton
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO


//    @Provides
//    @Singleton
//    fun provideAppDatabase(@ApplicationContext appContext: Context): NoteDataBase {
//        return Room.databaseBuilder(
//            appContext,
//            NoteDataBase::class.java,
//            "note_DB"
//        ).build()
//    }
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_photo)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

    )

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): JobDataBase {
        return Room.databaseBuilder(
            appContext,
            JobDataBase::class.java,
            "job_DB"
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }


    @Provides
    @Singleton
    fun providesMoshi():Moshi= Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor( ): HttpLoggingInterceptor {
        val localHttpLoggingInterceptor = HttpLoggingInterceptor()
        localHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return localHttpLoggingInterceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor:HttpLoggingInterceptor ):OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val builder: Request.Builder = chain.request().newBuilder()

            //    builder.addHeader(Constants.CONTENT_TYPE, Constants.APP_JSON)
                builder.method(original.method, original.body)
                chain.proceed(builder.build())
            }
            .addNetworkInterceptor(interceptor)
            .build()
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//
//       return OkHttpClient.Builder().addInterceptor(interceptor).build()


    @Provides
    @Singleton
    fun providesApiService(moshi: Moshi,okHttpClient: OkHttpClient): ApiJobService =

        Retrofit.Builder()
            .run {
                baseUrl(Constants.BASE_URL)
                client(okHttpClient)
                addConverterFactory(GsonConverterFactory.create())
                build()
            }.create(ApiJobService::class.java)
}