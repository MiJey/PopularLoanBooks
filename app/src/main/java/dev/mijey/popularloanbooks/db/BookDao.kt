package dev.mijey.popularloanbooks.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mijey.popularloanbooks.model.Book

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    @Query("SELECT * FROM books ORDER BY id")
    fun getBooks(): PagingSource<Int, Book>

    @Query("DELETE FROM books")
    suspend fun clearBooks()
}
