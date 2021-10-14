package dev.mijey.popularloanbooks.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.db.BookDatabase
import dev.mijey.popularloanbooks.model.Book
import kotlinx.coroutines.flow.Flow

class LibdataRepository(
    private val service: LibdataService,
    private val database: BookDatabase
) {
    fun getSearchResultStream(): Flow<PagingData<Book>> {
        val pagingSourceFactory = { database.booksDao().getBooks() }

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = LibdataRemoteMediator(service, database),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}
