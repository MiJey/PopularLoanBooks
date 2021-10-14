package dev.mijey.popularloanbooks.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dev.mijey.popularloanbooks.data.LibdataRemoteMediator.Companion.STARTING_PAGE_INDEX
import dev.mijey.popularloanbooks.data.LibdataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PopularLoanBooksViewModel(
    private val repository: LibdataRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    init {
        val initialPageIndex = savedStateHandle.get(LAST_PAGE_INDEX_KEY) ?: STARTING_PAGE_INDEX
        val lastPageIndexScrolled =
            savedStateHandle.get(LAST_PAGE_INDEX_SCROLLED_KEY) ?: STARTING_PAGE_INDEX
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
            .onStart { emit(UiAction.Scroll(currentPageIndex = lastPageIndexScrolled)) }

        state = requestFlow
            .flatMapLatest { request ->
                combine(
                    scrollFlow,
                    requestBook(),
                    ::Pair
                )
                    .distinctUntilChangedBy { it.second }
                    .map { (scroll, pagingData) ->
                        UiState(
                            lastPageIndexScrolled = scroll.currentPageIndex,
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

    private fun requestBook(): Flow<PagingData<UiModel>> =
        repository.getSearchResultStream()
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

    override fun onCleared() {
        savedStateHandle[LAST_PAGE_INDEX_KEY] = state.value.pageIndex
        savedStateHandle[LAST_PAGE_INDEX_SCROLLED_KEY] = state.value.lastPageIndexScrolled
        super.onCleared()
    }

    companion object {
        private const val LAST_PAGE_INDEX_KEY = "last_page_index"
        private const val LAST_PAGE_INDEX_SCROLLED_KEY = "last_page_index_scrolled"
    }
}
