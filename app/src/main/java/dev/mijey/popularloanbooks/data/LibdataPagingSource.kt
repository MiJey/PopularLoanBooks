package dev.mijey.popularloanbooks.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.data.LibdataRepository.Companion.NETWORK_PAGE_SIZE
import dev.mijey.popularloanbooks.model.Book
import dev.mijey.popularloanbooks.ui.PopularLoanBooksViewModel.Companion.DEFAULT_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException

class LibdataPagingSource(
    private val service: LibdataService,
    private val pageIndex: Int
) : PagingSource<Int, Book>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        return try {
            val position = params.key ?: DEFAULT_PAGE_INDEX
            val response = service.getPopularLoanBooks(
                pageIndex = position,
                pageSize = params.loadSize
            )

            // TODO error message
            val books = response.items[1].row ?: throw Exception("server error")
            val nextKey = if (books.isEmpty()) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }

            LoadResult.Page(
                data = books,
                prevKey = if (position == pageIndex) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
