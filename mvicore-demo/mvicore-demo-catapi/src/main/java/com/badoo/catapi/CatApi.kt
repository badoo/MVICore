package com.badoo.catapi

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface CatApi {

    @GET("images/get")
    fun getRandomImage(
        @Query("format") format: String = "xml",
        @Query("type") type: String = "jpg",
        @Query("size") size: String = "med",
        @Query("results_per_page") resultsPerPage: Int = 1
    ): Observable<Response>

    companion object {
        private var retrofit = Retrofit.Builder()
            .baseUrl("https://thecatapi.com/api/")
            .addCallAdapterFactory(
                RxJava3CallAdapterFactory.createWithScheduler(
                    Schedulers.io()
                )
            )
            .addConverterFactory(
                SimpleXmlConverterFactory.createNonStrict(
                    Persister(
                        AnnotationStrategy()
                    )
                ))
            .build()

        val service = retrofit.create(CatApi::class.java)
    }
}
