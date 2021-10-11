package dev.mijey.popularloanbooks.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.data.LibdataRepository
import dev.mijey.popularloanbooks.databinding.ActivityPopularLoanBooksBinding
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PopularLoanBooksActivity : AppCompatActivity() {
    private val viewModel: PopularLoanBooksViewModel by viewModels {
        PopularLoanBooksViewModelFactory(
            owner = this,
            repository = LibdataRepository(LibdataService.create()),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPopularLoanBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bindState(
            uiState = viewModel.state,
            uiActions = viewModel.accept
        )
    }

    private fun ActivityPopularLoanBooksBinding.bindState(
        uiState: StateFlow<UiState>,
        uiActions: (UiAction) -> Unit
    ) {
        val booksAdapter = BooksAdapter()
        list.adapter = booksAdapter.withLoadStateHeaderAndFooter(
            header = BooksLoadStateAdapter { booksAdapter.retry() },
            footer = BooksLoadStateAdapter { booksAdapter.retry() }
        )

        lifecycleScope.launch {
            uiState.map { it.pageIndex }
        }

        bindList(
            booksAdapter = booksAdapter,
            uiState = uiState,
            onScrollChanged = uiActions
        )
    }

    private fun ActivityPopularLoanBooksBinding.bindList(
        booksAdapter: BooksAdapter,
        uiState: StateFlow<UiState>,
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentPageIndex = uiState.value.pageIndex))
            }
        })
        val notLoading = booksAdapter.loadStateFlow
            .distinctUntilChangedBy { it.refresh }
            .map { it.refresh is LoadState.NotLoading }

        val hasNotScrolledForCurrentRequest = uiState
            .map { it.hasNotScrolledForCurrentRequest }
            .distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentRequest,
            Boolean::and
        )
            .distinctUntilChanged()

        val pagingData = uiState
            .map { it.pagingData }
            .distinctUntilChanged()

        lifecycleScope.launch {
            combine(shouldScrollToTop, pagingData, ::Pair)
                .distinctUntilChangedBy { it.second }
                .collectLatest { (shouldScroll, pagingData) ->
                    booksAdapter.submitData(pagingData)
                    if (shouldScroll) list.scrollToPosition(0)
                }
        }
    }
}