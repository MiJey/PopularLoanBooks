package dev.mijey.popularloanbooks.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.api.LibdataService
import dev.mijey.popularloanbooks.data.LibdataRepository
import dev.mijey.popularloanbooks.databinding.ActivityPopularLoanBooksBinding
import dev.mijey.popularloanbooks.db.BookDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PopularLoanBooksActivity : AppCompatActivity() {
    private val viewModel: PopularLoanBooksViewModel by viewModels {
        PopularLoanBooksViewModelFactory(
            owner = this,
            repository = LibdataRepository(
                service = LibdataService.create(),
                database = BookDatabase.getInstance(this)
            ),
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
        val header = BooksLoadStateAdapter { booksAdapter.retry() }
        val footer = BooksLoadStateAdapter { booksAdapter.retry() }

        list.adapter = booksAdapter.withLoadStateHeaderAndFooter(header, footer)

        bindList(
            header = header,
            booksAdapter = booksAdapter,
            uiState = uiState,
            onScrollChanged = uiActions
        )
    }

    private fun ActivityPopularLoanBooksBinding.bindList(
        header: BooksLoadStateAdapter,
        booksAdapter: BooksAdapter,
        uiState: StateFlow<UiState>,
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentPageIndex = uiState.value.lastPageIndexScrolled))
            }
        })

        retryButton.setOnClickListener {
            booksAdapter.retry()
        }

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

        lifecycleScope.launch {
            booksAdapter.loadStateFlow.collect { loadState ->
                header.loadState = loadState.mediator
                    ?.refresh
                    ?.takeIf { it is LoadState.Error && booksAdapter.itemCount > 0 }
                    ?: loadState.prepend

                emptyList.isVisible =
                    loadState.refresh is LoadState.NotLoading && booksAdapter.itemCount == 0
                list.isVisible =
                    loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
                progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading

                val errorState = loadState.mediator?.refresh as? LoadState.Error
                    ?: loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                if (errorState == null) {
                    errorMsg.isVisible = false
                    retryButton.isVisible = false
                } else {
                    errorMsg.text = "${errorState.error}"
                    errorMsg.isVisible = true
                    retryButton.isVisible = true
                }
            }
        }
    }
}
