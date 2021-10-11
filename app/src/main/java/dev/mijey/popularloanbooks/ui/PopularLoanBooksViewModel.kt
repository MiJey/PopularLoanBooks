package dev.mijey.popularloanbooks.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.mijey.popularloanbooks.data.LibdataRepository
import dev.mijey.popularloanbooks.model.Book
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

    private fun requestBook(pageIndex: Int): Flow<PagingData<Book>> =
        repository.getSearchResultStream(pageIndex)
            .cachedIn(viewModelScope)

    companion object {
        const val LAST_PAGE_SCROLLED: String = "last_page_scrolled"
        const val LAST_PAGE_INDEX: String = "last_page_index"
        const val DEFAULT_PAGE_INDEX = 1
    }
}
