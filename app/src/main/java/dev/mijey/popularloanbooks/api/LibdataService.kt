package dev.mijey.popularloanbooks.api

import dev.mijey.popularloanbooks.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LibdataService {
    @GET("Poplitloanbook")
    suspend fun getPopularLoanBooks(
        @Query("KEY") key: String = BuildConfig.LIBDATA_API_KEY,
        @Query("Type") type: String = "json",
        @Query("pIndex") pageIndex: Int,
        @Query("pSize") pageSize: Int
    ): LibdataResponse

    companion object {
        private const val BASE_URL = "https://openapi.gg.go.kr/"

        fun create(): LibdataService {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LibdataService::class.java)
        }
    }
}
