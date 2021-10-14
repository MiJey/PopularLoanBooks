package dev.mijey.popularloanbooks.ui

import androidx.paging.PagingData
import dev.mijey.popularloanbooks.data.LibdataRemoteMediator.Companion.STARTING_PAGE_INDEX

data class UiState(
    val pageIndex: Int = STARTING_PAGE_INDEX,
    val lastPageIndexScrolled: Int = STARTING_PAGE_INDEX,
    val hasNotScrolledForCurrentRequest: Boolean = false,
    val pagingData: PagingData<UiModel> = PagingData.empty()
)
