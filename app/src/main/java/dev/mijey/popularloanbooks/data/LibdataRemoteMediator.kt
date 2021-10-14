package dev.mijey.popularloanbooks.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.db.BookDatabase
import dev.mijey.popularloanbooks.db.RemoteKeys
import dev.mijey.popularloanbooks.model.Book
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class LibdataRemoteMediator(
    private val service: LibdataService,
    private val bookDatabase: BookDatabase
) : RemoteMediator<Int, Book>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Book>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val apiResponse = service.getPopularLoanBooks(
                pageIndex = page,
                pageSize = state.config.pageSize
            )

            val books = apiResponse.items[1].row ?: throw Exception("server error")
            val endOfPaginationReached = books.isEmpty()

            bookDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    bookDatabase.remoteKeysDao().clearRemoteKeys()
                    bookDatabase.booksDao().clearBooks()
                }

                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = books.map {
                    RemoteKeys(bookName = it.bookName, prevKey = prevKey, nextKey = nextKey)
                }
                bookDatabase.remoteKeysDao().insertAll(keys)
                bookDatabase.booksDao().insertAll(books)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Book>): RemoteKeys? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { book ->
            bookDatabase.remoteKeysDao().remoteKeysBookName(book.bookName)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Book>): RemoteKeys? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { book ->
            bookDatabase.remoteKeysDao().remoteKeysBookName(book.bookName)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Book>
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.bookName?.let { bookName ->
                bookDatabase.remoteKeysDao().remoteKeysBookName(bookName)
            }
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 1
    }
}