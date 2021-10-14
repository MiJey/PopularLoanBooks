package dev.mijey.popularloanbooks.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @SerializedName("STD_YM") val stdYearMonth: String,
    @SerializedName("RKI_NO") val rankNumber: String,
    @SerializedName("BOOK_NM_INFO") val bookName: String,
    @SerializedName("AUTHOR_NM_INFO") val authorName: String,
    @SerializedName("PUBLSHCMPY_NM") val publisherName: String,
    @SerializedName("PUBLCATN_YY") val publicationYear: Int,
    @SerializedName("VOLM_CNT") val volumeCount: Int,
    @SerializedName("BOOK_IMAGE_URL") val bookImageUrl: String,
)
