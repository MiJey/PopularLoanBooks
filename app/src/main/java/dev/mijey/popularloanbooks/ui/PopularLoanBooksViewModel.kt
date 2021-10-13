package dev.mijey.popularloanbooks.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.mijey.popularloanbooks.data.LibdataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class PopularLoanBooksViewModel(
    private val repository: LibdataRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    init {
        val initialPageIndex = savedStateHandle.get(LAST_PAGE_INDEX) ?: DEFAULT_PAGE_INDEX
        val lastPageScrolled = savedStateHandle.get(LAST_PAGE_SCROLLED) ?: DEFAULT_PAGE_INDEX
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val requestFlow = actionStateFlow
            .filterIsInstance<UiAction.Request>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Request(pageIndex = initialPageIndex)) }
        val scrollFlow = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(UiAction.Scroll(currentPageIndex = lastPageScrolled)) }

        state = requestFlow
            .flatMapLatest { request ->
                combine(
                    scrollFlow,
                    requestBook(pageIndex = request.pageIndex),
                    ::Pair
                )
                    .distinctUntilChangedBy { it.second }
                    .map { (scroll, pagingData) ->
                        UiState(
                            pageIndex = request.pageIndex,
                            lastPageScrolled = scroll.currentPageIndex,
                            hasNotScrolledForCurrentRequest = request.pageIndex != scroll.currentPageIndex,
                            pagingData = pagingData
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_PAGE_INDEX] = state.value.pageIndex
        savedStateHandle[LAST_PAGE_SCROLLED] = state.value.lastPageScrolled
        super.onCleared()
    }

    private fun requestBook(pageIndex: Int): Flow<PagingData<UiModel>> =
        repository.getSearchResultStream(pageIndex)
            .map { pagingData -> pagingData.map { UiModel.BookItem(it) } }
            .map {
                it.insertSeparators { before, after ->
                    after ?: return@insertSeparators null
                    before
                        ?: return@insertSeparators UiModel.SeparatorItem(after.roundedRank)

                    if (before.roundedRank < after.roundedRank) {
                        UiModel.SeparatorItem(after.roundedRank)
                    } else {
                        null
                    }
                }
            }
            .cachedIn(viewModelScope)

    private val UiModel.BookItem.roundedRank: Int
        // 1~100 -> TOP 100, 101~200 -> TOP 200
        get() = ((this.book.rankNumber.toInt() + 99) / 100) * 100

    companion object {
        const val LAST_PAGE_SCROLLED: String = "last_page_scrolled"
        const val LAST_PAGE_INDEX: String = "last_page_index"
        const val DEFAULT_PAGE_INDEX = 1
    }
}
