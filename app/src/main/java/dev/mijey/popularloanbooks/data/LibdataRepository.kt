package dev.mijey.popularloanbooks.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.model.Book
import kotlinx.coroutines.flow.Flow

class LibdataRepository(private val service: LibdataService) {
    fun getSearchResultStream(pageIndex: Int): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { LibdataPagingSource(service, pageIndex) }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}
