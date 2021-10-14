package dev.mijey.popularloanbooks.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val bookName: String,   // TODO bookName to id: Long
    val prevKey: Int?,
    val nextKey: Int?
)
