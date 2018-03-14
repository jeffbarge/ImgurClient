package com.barger.sofichallenge

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ImgurService {
    //If there was more than a single endpoint being used in this project, I'd probably
    //use an interceptor to add the auth header, but that feels like overkill here
    @Headers("Authorization: Client-ID 126701cd8332f32")
    @GET("gallery/search/time/{pageNumber}")
    fun searchImages(@Path("pageNumber") pageNumber: Int,
                     @Query("q") query: String): Observable<SearchResult>

    companion object {
        //In a project of any real size, I'd use Dagger to inject these instances
        //but where this is the only service being used in this project, I think this
        //is good enough
        fun create() : ImgurService {
            return Retrofit.Builder()
                    .baseUrl("https://api.imgur.com/3/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(JacksonConverterFactory.create(
                            jacksonObjectMapper()
                                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)))
                    .build()
                    .create(ImgurService::class.java)
        }
    }
}