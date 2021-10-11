package dev.mijey.popularloanbooks.api

import com.google.gson.annotations.SerializedName
import dev.mijey.popularloanbooks.model.Book

/**
 * 의미 없는 json 개체가 많아서 별도의 파일로 분리하지 않음
 */

data class LibdataResponse(
    @SerializedName("Poplitloanbook") val items: List<Poplitloanbook> = emptyList()
)

data class Poplitloanbook(
    @SerializedName("head") val head: List<PoplitloanbookHead>?,
    @SerializedName("row") val row: List<Book>?
)

data class PoplitloanbookHead(
    @SerializedName("list_total_count") val totalCount: Int?,
    @SerializedName("RESULT") val result: PoplitloanbookResult?,
    @SerializedName("api_version") val apiVersion: String?
)

data class PoplitloanbookResult(
    @SerializedName("CODE") val code: String,
    @SerializedName("MESSAGE") val message: String
)
