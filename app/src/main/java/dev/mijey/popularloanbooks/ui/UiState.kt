package dev.mijey.popularloanbooks.ui

import androidx.paging.PagingData
import dev.mijey.popularloanbooks.model.Book
import dev.mijey.popularloanbooks.ui.PopularLoanBooksViewModel.Companion.DEFAULT_PAGE_INDEX

data class UiState(
    val pageIndex: Int = DEFAULT_PAGE_INDEX,
    val lastPageScrolled: Int = DEFAULT_PAGE_INDEX,
    val hasNotScrolledForCurrentRequest: Boolean = false,
    val pagingData: PagingData<Book> = PagingData.empty()
)